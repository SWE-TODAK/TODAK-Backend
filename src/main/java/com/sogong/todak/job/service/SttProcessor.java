package com.sogong.todak.job.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sogong.todak.ai.AiClient;
import com.sogong.todak.ai.dto.AiSttData;
import com.sogong.todak.ai.dto.AiSttResponse;
import com.sogong.todak.ai.dto.SttByUrlRequest;
import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.entity.JobStatus;
import com.sogong.todak.job.entity.JobType;
import com.sogong.todak.job.repository.JobRepository;
import com.sogong.todak.recording.entity.Recording;
import com.sogong.todak.recording.repository.RecordingRepository;
import com.sogong.todak.recording.service.S3PresignService;
import com.sogong.todak.transcription.entity.Transcription;
import com.sogong.todak.transcription.repository.TranscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SttProcessor {

    private final JobRepository jobRepository;
    private final RecordingRepository recordingRepository;
    private final TranscriptionRepository transcriptionRepository;
    private final S3PresignService s3PresignService;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    /**
     * 1. 작업 선점 (별도 트랜잭션)
     */
    @Transactional
    public boolean startJob(UUID jobId) {
        int updated = jobRepository.claimJob(
                jobId,
                JobStatus.QUEUED,
                JobStatus.RUNNING
        );
        return updated == 1;
    }

    /**
     * 2. 실제 프로세스 (트랜잭션 없음 - 긴 외부 API 호출용)
     */
    public void process(UUID jobId) {
        try {
            // ID로 새로 조회하여 최신 상태 확보
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

            Recording recording = recordingRepository.findById(job.getRecordingId())
                    .orElseThrow(() -> new IllegalArgumentException("Recording not found: " + job.getRecordingId()));

            // S3 Presigned URL 생성
            String downloadUrl = s3PresignService.presignGetUrl(recording.getStorageKey());

            // AI STT API 호출 (네트워크 I/O)
            SttByUrlRequest request = new SttByUrlRequest(
                    recording.getRecordingId(),
                    "ko",
                    downloadUrl,
                    false, 25, 2, 250, 1.0
            );

            AiSttResponse response = aiClient.requestTranscriptionByUrl(request);
            AiSttData data = response.data();

            if (data == null) throw new IllegalStateException("AI response data is null");

            // 3. 결과 저장 (새 트랜잭션)
            saveResults(jobId, job.getRecordingId(), data);

        } catch (Exception e) {
            // 에러 발생 시 처리 (새 트랜잭션)
            handleFailure(jobId, e);
        }
    }

    /**
     * 3. 결과 반영 (별도 트랜잭션)
     */
    @Transactional
    public void saveResults(UUID jobId, UUID recordingId, AiSttData data) {
        Job job = jobRepository.findById(jobId).orElseThrow();
        Recording recording = recordingRepository.findById(recordingId).orElseThrow();

        // 중복 방지 로직
        if (transcriptionRepository.findByRecordingId(recordingId).isEmpty()) {
            transcriptionRepository.save(Transcription.create(
                    recordingId,
                    data.transcript(),
                    data.language(),
                    data.meta() != null ? toStringValue(data.meta().get("provider")) : null,
                    data.meta() != null ? toStringValue(data.meta().get("model")) : null,
                    toJson(data.meta())
            ));
        }

        // 다음 단계(SUMMARY) 작업 예약
        if (jobRepository.findByRecordingIdAndJobType(recordingId, JobType.SUMMARY).isEmpty()) {
            jobRepository.save(Job.create(recordingId, JobType.SUMMARY));
        }

        job.markSucceeded();
        recording.markDone();
        log.info("STT job succeeded. jobId={}, recordingId={}", jobId, recordingId);
    }

    /**
     * 4. 실패 처리 (별도 트랜잭션)
     */
    @Transactional
    public void handleFailure(UUID jobId, Exception e) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.markFailed(truncate(e.getMessage()));
            recordingRepository.findById(job.getRecordingId())
                    .ifPresent(Recording::markFailed);
            log.error("STT job failed. jobId={}, recordingId={}", jobId, job.getRecordingId(), e);
        });
    }

    private String toStringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String toJson(Object value) {
        try {
            return value == null ? null : objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"meta_json_serialize_failed\"}";
        }
    }

    private String truncate(String message) {
        if (message == null) return "Unknown error";
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}