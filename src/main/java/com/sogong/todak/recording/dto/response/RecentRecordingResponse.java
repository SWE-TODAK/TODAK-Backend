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
                // Summary 엔티티가 있을 때만 intro 추출
                .intro(recording.getSummary() != null ? recording.getSummary().getIntro() : null)
                .title(null) // 명세서 요구사항에 따라 null 설정
                .build();
    }
}