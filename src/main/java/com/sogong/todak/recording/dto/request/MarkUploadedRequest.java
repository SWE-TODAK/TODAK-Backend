package com.sogong.todak.recording.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MarkUploadedRequest {
    @NotBlank
    private String storageKey;

    private Integer durationMs;
    private Integer sampleRate;
    private String mimeType;
}