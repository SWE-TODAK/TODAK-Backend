package com.sogong.todak.recording.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateRecordingUploadRequest {
    @NotBlank
    private String mimeType; // 예: "audio/wav", "audio/m4a"
}