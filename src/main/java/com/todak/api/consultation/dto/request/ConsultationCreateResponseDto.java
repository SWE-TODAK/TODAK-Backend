package com.todak.api.consultation.dto.request;

import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationCreateResponseDto {

    private Long consultationId;

    private Long appointmentId;

    private String hospitalName;

    private OffsetDateTime consultationTime;
}
