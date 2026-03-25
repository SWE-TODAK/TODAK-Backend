package com.sogong.todak.job.worker;

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
        // 1. 작업이 없으면 즉시 리턴 (로그 한 줄도 안 남음)
        if (jobs.isEmpty()) return;

        // 2. 작업이 있을 때만 딱 한 줄 기록
        //log.info("Starting STT Batch: {} jobs found.", jobs.size());

        for (Job job : jobs) {
            try {
                processSingleJob(job.getJobId());
            } catch (Exception e) {
                //log.error("Failed to process STT job. jobId={}", job.getJobId(), e);
            }
        }
    }

    @Transactional
    public void processSingleJob(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        //log.info("Picked STT job. jobId={}, recordingId={}", job.getJobId(), job.getRecordingId());

        if (job.getJobType() != JobType.STT || job.getStatus() != JobStatus.QUEUED) {
            return;
        }

        Recording recording = recordingRepository.findById(job.getRecordingId())
                .orElseThrow(() -> new IllegalArgumentException("Recording not found"));

        try {
            job.markRunning();

            String downloadUrl = s3PresignService.presignGetUrl(recording.getStorageKey());
            //log.debug("Generated download URL for recordingId={}", recording.getRecordingId());

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
            //log.debug("Calling AI STT server. recordingId={}", recording.getRecordingId());

            AiSttData data = response.data();
            //log.debug("Received AI STT response. recordingId={}", recording.getRecordingId());

            if (data == null) {
                throw new IllegalStateException("AI response data is null");
            }

            //log.debug("Saving transcription. recordingId={}", recording.getRecordingId());
            String transcript = data.transcript();
            if (transcript == null || transcript.isBlank()) {
                throw new IllegalStateException("Transcript is empty");
            }

            String language = data.language();
            Map<String, Object> meta = data.meta();

            String provider = meta != null ? toStringValue(meta.get("provider")) : null;
            String model = meta != null ? toStringValue(meta.get("model")) : null;
            String metaJson = toJson(meta);

            var existing = transcriptionRepository.findByRecordingId(recording.getRecordingId());

            if (existing.isPresent()) {
                //log.warn("Transcription already exists. recordingId={}", recording.getRecordingId());
            } else {
                transcriptionRepository.save(
                        Transcription.create(
                                recording.getRecordingId(),
                                transcript,
                                language,
                                provider,
                                model,
                                metaJson
                        )
                );
            }

            job.markSucceeded();
            recording.markDone();
            log.info("STT job succeeded. jobId={}, recordingId={}", job.getJobId(), recording.getRecordingId());

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