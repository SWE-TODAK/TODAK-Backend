package com.sogong.todak.recording.controller;

import com.sogong.todak.recording.dto.request.CreateRecordingUploadRequest;
import com.sogong.todak.recording.dto.request.MarkUploadedRequest;
import com.sogong.todak.recording.dto.response.CreateRecordingUploadResponse;
import com.sogong.todak.recording.service.RecordingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recordings")
public class RecordingController {

    private final RecordingService recordingService;

    @PostMapping("/upload")
    public ResponseEntity<CreateRecordingUploadResponse> createUpload(
            @Valid @RequestBody CreateRecordingUploadRequest req
    ) {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(recordingService.createUpload(userId, req));
    }

    @PostMapping("/{recordingId}/uploaded")
    public ResponseEntity<Void> markUploaded(
            @PathVariable UUID recordingId,
            @Valid @RequestBody MarkUploadedRequest req
    ) {
        UUID userId = getCurrentUserId();
        recordingService.markUploaded(userId, recordingId, req);
        return ResponseEntity.ok().build();
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Unauthenticated");
        }

        // 대부분의 JWT 설정에서 authentication.getName() == userId(UUID 문자열)
        return UUID.fromString(authentication.getName());
    }
}