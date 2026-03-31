package com.sogong.todak.job.worker;

import com.sogong.todak.ai.AiClient;
import com.sogong.todak.ai.dto.AiSummaryResponse;
import com.sogong.todak.ai.dto.SummaryRequest;
import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.entity.JobStatus;
import com.sogong.todak.job.entity.JobType;
import com.sogong.todak.job.repository.JobRepository;
import com.sogong.todak.recording.repository.RecordingRepository; // Recording 조회를 위해 필요
import com.sogong.todak.summary.entity.Summary;
import com.sogong.todak.summary.repository.SummaryRepository;
import com.sogong.todak.transcription.repository.TranscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SummaryJobWorker {

    private final JobRepository jobRepository;
    private final TranscriptionRepository transcriptionRepository;
    private final SummaryRepository summaryRepository;
    private final AiClient aiClient;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processSummaryJobs() {
        List<Job> jobs = jobRepository.findTop10ByJobTypeAndStatusOrderByCreatedAtAsc(
                JobType.SUMMARY, JobStatus.QUEUED);

        for (Job job : jobs) {
            processSingleJob(job);
        }
    }

    private void processSingleJob(Job job) {
        try {
            job.markRunning();
            jobRepository.save(job);

            UUID recordingId = job.getRecordingId();

            // 1. STT 결과 가져오기 (1:1 매핑이므로 recordingId로 바로 조회)
            var transcription = transcriptionRepository.findByRecordingId(recordingId)
                    .orElseThrow(() -> new RuntimeException("STT 결과가 없습니다. ID: " + recordingId));

            // 2. FastAPI 요약 요청 (consultationId 제외)
            SummaryRequest request = new SummaryRequest(
                    recordingId,
                    transcription.getTranscriptText()
            );

            AiSummaryResponse response = aiClient.requestSummary(request);

            // 3. 요약 데이터 저장 (보내주신 V5 SQL 구조에 딱 맞음)
            Summary summary = Summary.create(
                    recordingId,
                    response.data().intro(),
                    response.data().content()
            );
            summaryRepository.save(summary);

            job.markSucceeded();
            log.info("요약 완료 - Recording ID: {}", recordingId);

        } catch (Exception e) {
            log.error("요약 실패 - Recording ID: {}, Error: {}", job.getRecordingId(), e.getMessage());
            job.markFailed(e.getMessage());
        } finally {
            jobRepository.save(job);
        }
    }
}