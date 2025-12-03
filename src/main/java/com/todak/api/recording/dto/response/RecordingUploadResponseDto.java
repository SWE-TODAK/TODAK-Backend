package com.todak.api.recording.dto.response;

import com.todak.api.recording.entity.Recording;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecordingUploadResponseDto {

    private Long recordingId;
    private Long consultationId;
    private String filePath;
    private String status;
    private String createdAt;

    public static RecordingUploadResponseDto from(Recording r) {
        return RecordingUploadResponseDto.builder()
                .recordingId(r.getRecordingId())
                .consultationId(r.getConsultation().getConsultationId())
                .filePath(r.getFilePath())
                .status(r.getStatus().name())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : null)
                .build();
    }
}
