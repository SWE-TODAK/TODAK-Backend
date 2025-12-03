package com.todak.api.hospital.dto.response;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalDetailResponseDto {

    private Long hospitalId;
    private String name;
    private String address;
    private String phone;
    private String introduction;

    private List<String> categories;
    private Boolean favorite;

    private List<DoctorDto> doctors;

    private AvailableHoursDto availableHours;
}
