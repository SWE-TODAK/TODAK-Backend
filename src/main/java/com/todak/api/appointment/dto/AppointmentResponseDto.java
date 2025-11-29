package com.todak.api.hospital.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponseDto {

    private Long appointmentId;

    private String patientId;       // UUID

    // 병원 정보
    private Long hospitalId;
    private String hospitalName;

    // 의사 정보 (선택적)
    private Long doctorId;
    private String doctorName;

    // 진료과 정보 (의사 미선택 시)
    private Long departmentId;
    private String departmentName;

    private LocalDateTime dateTime;

    private String status;          // REQUESTED / APPROVED / CANCELLED / DONE

    private LocalDateTime updatedAt;
}