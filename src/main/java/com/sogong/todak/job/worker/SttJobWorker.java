package com.sogong.todak.job.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sogong.todak.ai.AiClient;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SttJobWorker {

    private final JobRepository jobRepository;
    private final RecordingRepository recordingRepository;
    private final TranscriptionRepository transcriptionRepository;
    private final S3PresignService s3PresignService;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    public void processQueuedSttJobs() {
        List<Job> jobs = jobRepository.findTop10ByJobTypeAndStatusOrderByCreatedAtAsc(
                JobType.STT,
                JobStatus.QUEUED
        );

        for (Job job : jobs) {
            try {
                processSingleJob(job.getJobId());
            } catch (Exception e) {
                log.error("Failed to process STT job. jobId={}", job.getJobId(), e);
            }
        }
    }

    @Transactional
    public void processSingleJob(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (job.getJobType() != JobType.STT || job.getStatus() != JobStatus.QUEUED) {
            return;
        }

        Recording recording = recordingRepository.findById(job.getRecordingId())
                .orElseThrow(() -> new IllegalArgumentException("Recording not found"));

        try {
            job.markRunning();

            String downloadUrl = s3PresignService.presignGetUrl(recording.getStorageKey());

            SttByUrlRequest request = new SttByUrlRequest(
                    recording.getRecordingId(),
                    null,
                    "ko",
                    downloadUrl,
                    false,
                    25,
                    2,
                    250,
                    1.0
            );

            AiSttResponse response = aiClient.requestTranscriptionByUrl(request);

            Map<String, Object> data = response.data();
            if (data == null) {
                throw new IllegalStateException("AI response data is null");
            }

            String transcript = toStringValue(data.get("transcript"));
            String language = toStringValue(data.get("language"));

            @SuppressWarnings("unchecked")
            Map<String, Object> meta = (Map<String, Object>) data.get("meta");

            String provider = meta != null ? toStringValue(meta.get("provider")) : null;
            String model = meta != null ? toStringValue(meta.get("model")) : null;
            String metaJson = toJson(meta);

            transcriptionRepository.findByRecordingId(recording.getRecordingId())
                    .ifPresentOrElse(
                            existing -> {
                                throw new IllegalStateException("Transcription already exists");
                            },
                            () -> transcriptionRepository.save(
                                    Transcription.create(
                                            recording.getRecordingId(),
                                            transcript,
                                            language,
                                            provider,
                                            model,
                                            metaJson
                                    )
                            )
                    );

            job.markSucceeded();
            recording.markDone();

        } catch (Exception e) {
            job.markFailed(truncate(e.getMessage()));
            recording.markFailed();
            log.error("STT job failed. jobId={}, recordingId={}", job.getJobId(), recording.getRecordingId(), e);
        }
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