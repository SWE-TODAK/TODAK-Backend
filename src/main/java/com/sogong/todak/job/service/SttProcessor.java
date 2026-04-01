package com.sogong.todak.job.service;

import com.sogong.todak.ai.AiClient;
import com.sogong.todak.ai.dto.AiSttData;
import com.sogong.todak.ai.dto.AiSttResponse;
import com.sogong.todak.ai.dto.SttByUrlRequest;
import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.repository.JobRepository;
import com.sogong.todak.recording.entity.Recording;
import com.sogong.todak.recording.repository.RecordingRepository;
import com.sogong.todak.recording.service.S3PresignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SttProcessor {
    private final JobService jobService;
    private final JobRepository jobRepository;
    private final RecordingRepository recordingRepository;
    private final S3PresignService s3PresignService;
    private final AiClient aiClient;

    public void process(UUID jobId) {
        // 1. 선점 시도 (Atomic Update)
        if (!jobService.claimJob(jobId)) return;

        try {
            // 2. 데이터 준비
            Job job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job 없음"));
            Recording rec = recordingRepository.findById(job.getRecordingId()).orElseThrow(() -> new RuntimeException("녹음 없음"));
            String downloadUrl = s3PresignService.presignGetUrl(rec.getStorageKey());

            // 3. API 호출
            AiSttResponse response = aiClient.requestTranscriptionByUrl(new SttByUrlRequest(
                    rec.getRecordingId(), "ko", downloadUrl, false, 25, 2, 250, 1.0));

            // 4. [검증 및 Throw]
            AiSttData data = response.data();
            if (data == null) throw new RuntimeException("AI 응답 데이터가 null입니다.");

            String aiStatus = String.valueOf(data.meta().getOrDefault("jobStatus", "SUCCESS"));
            if ("FAILED".equals(aiStatus)) {
                throw new RuntimeException("AI 서버 처리 실패: " + data.meta().get("error"));
            }

            // 5. 결과 저장 (DB)
            jobService.completeSttJob(jobId, job.getRecordingId(), data);

        } catch (Exception e) {
            jobService.failJob(jobId, e);
        }
    }
}