package com.sogong.todak.recording.dto.response;

import com.sogong.todak.recording.entity.Recording;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@Builder
public class RecentRecordingResponse {
    private UUID recordingId;
    private String date;
    private String intro;
    private String title;

    public static RecentRecordingResponse from(Recording recording) {
        return RecentRecordingResponse.builder()
                .recordingId(recording.getRecordingId())
                .date((recording.getConsultedAt() != null ? recording.getConsultedAt() : recording.getCreatedAt())
                        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .intro(recording.getSummary() != null ? recording.getSummary().getIntro() : null)
                .title(recording.getTitle())
                .build();
    }
}