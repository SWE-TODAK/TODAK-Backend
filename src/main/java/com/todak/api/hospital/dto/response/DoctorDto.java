package com.todak.api.hospital.dto.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorDto {

    private Long doctorId;
    private String name;
    private String specialty;
    private Long mainDepartmentId; // 의사의 주력 진료과
}
