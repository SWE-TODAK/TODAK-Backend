package com.sogong.todak.recording.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CreateRecordingUploadResponse {
    private UUID recordingId;
    private String storageKey;
    private String uploadUrl; // presigned PUT url
    private String method;    // "PUT"
    private String mimeType;
}