package com.sogong.todak.recording.service;

import com.sogong.todak.recording.service.S3PresignService;
import com.sogong.todak.recording.dto.request.CreateRecordingUploadRequest;
import com.sogong.todak.recording.dto.request.MarkUploadedRequest;
import com.sogong.todak.recording.dto.response.CreateRecordingUploadResponse;
import com.sogong.todak.recording.entity.Recording;
import com.sogong.todak.recording.repository.RecordingRepository;
import com.sogong.todak.job.dto.response.JobResponse;
import com.sogong.todak.job.entity.Job;
import com.sogong.todak.job.entity.JobType;
import com.sogong.todak.job.repository.JobRepository;
import com.sogong.todak.recording.entity.RecordingStatus;
import com.sogong.todak.recording.dto.request.UpdateRecordingMetadataRequest;
import com.sogong.todak.recording.dto.response.RecordingDetailResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordingService {

    private final RecordingRepository recordingRepository;
    private final S3PresignService s3PresignService;
    private final JobRepository jobRepository;

    @Value("${aws.s3.prefix:recordings}")
    private String prefix;
    private String resolveTitle(String title, OffsetDateTime consultedAt) {
        if (title != null && !title.isBlank()) {
            return title.trim();
        }

        OffsetDateTime base = (consultedAt != null) ? consultedAt : OffsetDateTime.now();
        return base.toLocalDate() + " 진료";
    }

    @Transactional
    public CreateRecordingUploadResponse createUpload(UUID userId, CreateRecordingUploadRequest req) {
        Recording recording = Recording.create(userId);
        recordingRepository.save(recording);

        // storageKey 규칙: recordings/{userId}/{recordingId}.wav 처럼
        // mimeType에 따라 확장자 선택(간단히 wav로 고정해도 됨)
        String ext = guessExt(req.getMimeType()); // 아래 메서드 참고
        String key = prefix + "/" + userId + "/" + recording.getRecordingId() + "." + ext;

        String uploadUrl = s3PresignService.presignPutUrl(key, req.getMimeType());

        return CreateRecordingUploadResponse.builder()
                .recordingId(recording.getRecordingId())
                .storageKey(key)
                .uploadUrl(uploadUrl)
                .method("PUT")
                .mimeType(req.getMimeType())
                .build();
    }

    @Transactional
    public RecordingDetailResponse updateMetadata(UUID userId, UUID recordingId, UpdateRecordingMetadataRequest req) {
        Recording recording = recordingRepository.findByRecordingIdAndUserId(recordingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found"));

        OffsetDateTime finalConsultedAt = (req.getConsultedAt() != null)
                ? req.getConsultedAt()
                : OffsetDateTime.now();

        String finalTitle = resolveTitle(req.getTitle(), finalConsultedAt);

        recording.updateMedicalMetadata(
                req.getHospitalName(),
                req.getDiseaseName(),
                req.getDoctorName(),
                req.getDepartmentName(),
                finalConsultedAt,
                finalTitle
        );

        return RecordingDetailResponse.from(recording);
    }

    @Transactional
    public void markUploaded(UUID userId, UUID recordingId, MarkUploadedRequest req) {
        Recording recording = recordingRepository.findByRecordingIdAndUserId(recordingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found"));

        // storageKey가 응답에서 준 것과 동일한지 체크(보안)
        recording.markUploaded(req.getStorageKey(), req.getMimeType(), req.getDurationMs(), req.getSampleRate());
    }

    private String guessExt(String mimeType) {
        // 너희 앱이 wav 고정이면 그냥 "wav" 반환해도 됨
        if (mimeType == null) return "wav";
        return switch (mimeType) {
            case "audio/wav", "audio/wave", "audio/x-wav" -> "wav";
            case "audio/m4a", "audio/mp4" -> "m4a";
            case "audio/mpeg" -> "mp3";
            default -> "wav";
        };
    }

    public JobResponse startStt(UUID userId, UUID recordingId) {

        var recording = recordingRepository
                .findByRecordingIdAndUserId(recordingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found"));

        if (recording.getStatus() != RecordingStatus.UPLOADED) {
            throw new IllegalStateException("Recording is not uploaded yet");
        }

        var existingJob = jobRepository
                .findByRecordingIdAndJobType(recordingId, JobType.STT);

        if (existingJob.isPresent()) {
            return JobResponse.from(existingJob.get());
        }

        Job job = Job.create(recordingId, JobType.STT);
        jobRepository.save(job);

        recording.markProcessing();

        return JobResponse.from(job);
    }
}