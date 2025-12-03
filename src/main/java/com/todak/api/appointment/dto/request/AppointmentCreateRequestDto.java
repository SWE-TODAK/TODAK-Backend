package com.todak.api.appointment.dto.request;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCreateRequestDto {

    private Long hospitalId;        // 필수

    private Long doctorId;          // 선택

    private LocalDateTime datetime; // 예약 시간
}
