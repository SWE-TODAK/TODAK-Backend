package com.todak.api.consultation.controller;

import com.todak.api.consultation.dto.response.*;
import com.todak.api.consultation.dto.request.*;
import com.todak.api.consultation.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    /**
     * 1) 진료 생성
     * POST /api/consultations/start?appointmentId=10
     */
    @PostMapping("/start")
    public ResponseEntity<ConsultationCreateResponseDto> startConsultation(
            @AuthenticationPrincipal Long kakaoId,
            @RequestParam Long appointmentId
    ) {
        ConsultationCreateResponseDto response =
                consultationService.startConsultation(appointmentId, kakaoId);

        return ResponseEntity.ok(response);
    }

    /**
     * 2) 진료 상세 조회
     * GET /api/consultations/{consultationId}
     */
    @GetMapping("/{consultationId}")
    public ResponseEntity<ConsultationDetailResponseDto> getConsultationDetail(
            @AuthenticationPrincipal Long kakaoId,
            @PathVariable Long consultationId
    ) {
        ConsultationDetailResponseDto response =
                consultationService.getConsultationDetail(consultationId);

        return ResponseEntity.ok(response);
    }

    /**
     * 3) 나의 전체 진료 이력 조회
     * GET /api/consultations/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<ConsultationListResponseDto>> getMyConsultations(
            @AuthenticationPrincipal Long kakaoId
    ) {
        List<ConsultationListResponseDto> response =
                consultationService.getMyConsultations(kakaoId);

        return ResponseEntity.ok(response);
    }

    /**
     * 4) 날짜별 진료 조회 (캘린더)
     * GET /api/consultations/my/date?date=2025-11-29
     */
    @GetMapping("/my/date")
    public ResponseEntity<List<ConsultationListResponseDto>> getMyConsultationsByDate(
            @AuthenticationPrincipal Long kakaoId,
            @RequestParam String date
    ) {
        LocalDate parsedDate = LocalDate.parse(date);

        List<ConsultationListResponseDto> response =
                consultationService.getConsultationsByDate(kakaoId, parsedDate);

        return ResponseEntity.ok(response);
    }
}
