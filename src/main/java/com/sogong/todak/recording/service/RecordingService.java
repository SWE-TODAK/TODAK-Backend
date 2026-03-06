package com.sogong.todak.recording.service;

import com.sogong.todak.recording.service.S3PresignService;
import com.sogong.todak.recording.dto.request.CreateRecordingUploadRequest;
import com.sogong.todak.recording.dto.request.MarkUploadedRequest;
import com.sogong.todak.recording.dto.response.CreateRecordingUploadResponse;
import com.sogong.todak.recording.entity.Recording;
import com.sogong.todak.recording.repository.RecordingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordingService {

    private final RecordingRepository recordingRepository;
    private final S3PresignService s3PresignService;

    @Value("${aws.s3.prefix:recordings}")
    private String prefix;

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
}