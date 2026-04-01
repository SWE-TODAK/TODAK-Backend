package com.sogong.todak.job.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sogong.todak.ai.dto.AiSttData;
import com.sogong.todak.ai.dto.AiSummaryResponse;
import com.sogong.todak.job.dto.response.JobResponse;
import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.entity.JobStatus;
import com.sogong.todak.job.entity.JobType;
import com.sogong.todak.job.repository.JobRepository;
import com.sogong.todak.recording.entity.Recording;
import com.sogong.todak.summary.entity.Summary;
import com.sogong.todak.transcription.entity.Transcription;
import com.sogong.todak.transcription.repository.TranscriptionRepository;
import com.sogong.todak.summary.repository.SummaryRepository;
import com.sogong.todak.recording.repository.RecordingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.flywaydb.core.internal.util.JsonUtils.toJson;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final RecordingRepository recordingRepository;
    private final TranscriptionRepository transcriptionRepository;
    private final SummaryRepository summaryRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public boolean claimJob(UUID jobId) {
        // DB 쿼리 레벨에서 QUEUED -> RUNNING으로 원자적 변경
        return jobRepository.claimJob(jobId, JobStatus.QUEUED, JobStatus.RUNNING) == 1;
    }

    @Transactional
    public void completeSttJob(UUID jobId, UUID recordingId, AiSttData data) {
        // Transcription 저장
        if (transcriptionRepository.findByRecordingId(recordingId).isEmpty()) {
            transcriptionRepository.save(Transcription.create(
                    recordingId, data.transcript(), data.language(),
                    String.valueOf(data.meta().get("provider")), String.valueOf(data.meta().get("model")),
                    toJson(data.meta())
            ));
        }

        // 다음 작업(SUMMARY) 예약
        if (jobRepository.findByRecordingIdAndJobType(recordingId, JobType.SUMMARY).isEmpty()) {
            jobRepository.save(Job.create(recordingId, JobType.SUMMARY));
        }

        jobRepository.findById(jobId).ifPresent(Job::markSucceeded);
        recordingRepository.findById(recordingId).ifPresent(Recording::markDone);
    }

    @Transactional
    public void completeSummaryJob(UUID jobId, UUID recordingId, AiSummaryResponse response) {
        summaryRepository.findByRecordingId(recordingId).ifPresentOrElse(
                existing -> existing.update(response.data().intro(), response.data().content()),
                () -> summaryRepository.save(Summary.create(recordingId, response.data().intro(), response.data().content()))
        );

        jobRepository.findById(jobId).ifPresent(Job::markSucceeded);
    }

    @Transactional
    public void failJob(UUID jobId, Exception e) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.markFailed(truncate(e.getMessage()));
            if (job.getJobType() == JobType.STT) {
                recordingRepository.findById(job.getRecordingId()).ifPresent(Recording::markFailed);
            }
            log.error("Job 실패 처리됨: {}, 사유: {}", jobId, e.getMessage());
        });
    }

    private String toJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception e) { return "{}"; }
    }

    private String truncate(String str) {
        return (str != null && str.length() > 1000) ? str.substring(0, 1000) : str;
    }
}