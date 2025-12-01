package com.todak.api.hospital.controller;

import com.todak.api.hospital.dto.response.HospitalDetailResponseDto;
import com.todak.api.hospital.dto.response.HospitalListResponseDto;
import com.todak.api.hospital.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    /**
     * 1. 병원 목록 조회 (검색, 진료과 필터링)
     * GET /api/hospitals?search=강남&department=내과
     */
    @GetMapping
    public List<HospitalListResponseDto> getHospitalList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department
    ) {
        return hospitalService.searchHospitals(search, department);
    }

    /**
     * 2. 병원 상세 조회
     * GET /api/hospitals/{id}
     */
    @GetMapping("/{id}")
    public HospitalDetailResponseDto getHospitalDetail(@PathVariable Long id) {
        return hospitalService.getHospitalDetail(id);
    }

    /**
     * 3. 찜 토글
     * POST /api/hospitals/{id}/favorite?userId=abc123
     * (실제로는 userId는 헤더(JWT)에서 받게 됨)
     */
    @PostMapping("/{id}/favorite")
    public String toggleFavorite(
            @PathVariable Long id,
            @RequestParam String userId // TODO: JWT 적용 후 제거
    ) {
        hospitalService.toggleFavorite(id, userId);
        return "찜 상태가 변경되었습니다.";
    }
}
