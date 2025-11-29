package com.todak.api.hospital.service;

import com.todak.api.hospital.dto.response.HospitalDetailResponseDto;
import com.todak.api.hospital.dto.response.HospitalListResponseDto;

import java.util.List;

public interface HospitalService {

    List<HospitalListResponseDto> searchHospitals(String search, String department);

    HospitalDetailResponseDto getHospitalDetail(Long id);

    void toggleFavorite(Long hospitalId, String userId);
    // userId 필요 없으면 Long hospitalId만 받는 방식으로 바꿔도 됨
}
