package com.todak.api.summary.dto.response;

import com.todak.api.summary.entity.Summary;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class SummaryResponseDto {

    private Long summaryId;
    private Long consultationId;
    private Long recordingId;
    private String content;
    private OffsetDateTime createdAt;

    public static SummaryResponseDto from(Summary summary) {
        return SummaryResponseDto.builder()
                .summaryId(summary.getSummaryId())
                .consultationId(summary.getConsultationId())
                .recordingId(summary.getRecordingId())
                .content(summary.getContent())
                .createdAt(summary.getCreatedAt())
                .build();
    }
}
