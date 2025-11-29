package com.todak.api.hospital.dto.response;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalListResponseDto {

    private Long hospitalId;
    private String name;
    private String address;
    private List<String> categories; // ["내과","안과"]
    private boolean favorite;
}
