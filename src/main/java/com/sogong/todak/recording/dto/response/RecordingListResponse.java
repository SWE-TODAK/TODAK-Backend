package com.sogong.todak.recording.dto.response;

import com.sogong.todak.recording.entity.Recording;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class RecordingListResponse {
    private UUID recordingId;
    private String title;
    private OffsetDateTime createdAt;
    private String status;

    public static RecordingListResponse from(Recording recording) {
        return RecordingListResponse.builder()
                .recordingId(recording.getRecordingId())
                .title(recording.getTitle())
                .createdAt(recording.getCreatedAt())
                .status(recording.getStatus().name())
                .build();
    }
}