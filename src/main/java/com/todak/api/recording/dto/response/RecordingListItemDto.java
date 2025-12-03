package com.todak.api.recording.dto.response;

import com.todak.api.recording.entity.Recording;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecordingListItemDto {

    private Long recordingId;
    private Long consultationId;

    private String filePath;
    private Integer durationSeconds;

    private String status;
    private String createdAt;

    public static RecordingListItemDto from(Recording r) {
        return RecordingListItemDto.builder()
                .recordingId(r.getRecordingId())
                .consultationId(r.getConsultation().getConsultationId())
                .filePath(r.getFilePath())
                .durationSeconds(r.getDurationSeconds())
                .status(r.getStatus().name())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : null)
                .build();
    }
}
