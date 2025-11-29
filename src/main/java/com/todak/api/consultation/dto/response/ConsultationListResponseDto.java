package com.todak.api.consultation.dto.response;

import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ConsultationListResponseDto {

    private Long consultationId;

    private String hospitalName;

    private String doctorName;

    private OffsetDateTime consultationTime; // startedAt (예약 시간 그대로)

    private String summaryPreview;   // 요약 1~2줄 미리보기 (없으면 null)
}
