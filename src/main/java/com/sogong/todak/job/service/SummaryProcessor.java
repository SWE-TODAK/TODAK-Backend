package com.sogong.todak.job.service;

import com.sogong.todak.ai.AiClient;
import com.sogong.todak.ai.dto.AiSummaryResponse;
import com.sogong.todak.ai.dto.SummaryRequest;
import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.entity.JobStatus;
import com.sogong.todak.job.repository.JobRepository;
import com.sogong.todak.summary.entity.Summary;
import com.sogong.todak.summary.repository.SummaryRepository;
import com.sogong.todak.transcription.repository.TranscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryProcessor {

    private final JobRepository jobRepository;
    private final TranscriptionRepository transcriptionRepository;
    private final SummaryRepository summaryRepository;
    private final AiClient aiClient;

    @Transactional
    public boolean startJob(UUID jobId) {
        return jobRepository.claimJob(jobId, JobStatus.QUEUED, JobStatus.RUNNING) == 1;
    }

    public void process(UUID jobId) {
        try {
            Job job = jobRepository.findById(jobId).orElseThrow();
            UUID recordingId = job.getRecordingId();

            // 1. STT 결과 조회
            var transcription = transcriptionRepository.findByRecordingId(recordingId)
                    .orElseThrow(() -> new RuntimeException("STT 결과가 없습니다. ID: " + recordingId));

            log.info(">>>> [Summary] AI 서버 요청 준비 완료. recordingId: {}", recordingId);

            // 2. FastAPI 요약 요청 (트랜잭션 외부)
            SummaryRequest request = new SummaryRequest(recordingId, transcription.getTranscriptText());
            AiSummaryResponse response = aiClient.requestSummary(request);

            log.info(">>>> [Summary] AI 서버 응답 수신 완료!");

            // 3. 결과 저장 (새 트랜잭션)
            saveResults(jobId, recordingId, response);

        } catch (Exception e) {
            log.error(">>>> [Summary] 에러 발생: ", e); // 에러 로그 필수!
            handleFailure(jobId, e);
        }
    }

    @Transactional
    public void saveResults(UUID jobId, UUID recordingId, AiSummaryResponse response) {
        Job job = jobRepository.findById(jobId).orElseThrow();

        // 중복 저장 방지
        if (summaryRepository.findByRecordingId(recordingId).isEmpty()) {
            summaryRepository.save(Summary.create(
                    recordingId,
                    response.data().intro(),
                    response.data().content()
            ));
        }

        job.markSucceeded();
        log.info("요약 완료 - Recording ID: {}", recordingId);
    }

    @Transactional
    public void handleFailure(UUID jobId, Exception e) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.markFailed(e.getMessage());
            log.error("요약 실패 - Job ID: {}, Error: {}", jobId, e.getMessage());
        });
    }
}