package com.todak.api.consultation.controller;

import com.todak.api.consultation.dto.response.*;
import com.todak.api.consultation.dto.request.*;
import com.todak.api.consultation.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    /**
     * 1) 진료 생성
     * POST /api/v1/consultations/start?appointmentId=10&patientId=uuid
     */
    @PostMapping("/start")
    public ResponseEntity<ConsultationCreateResponseDto> startConsultation(
            @RequestParam Long appointmentId,
            @RequestParam UUID patientId
    ) {
        ConsultationCreateResponseDto response =
                consultationService.startConsultation(appointmentId, patientId);

        return ResponseEntity.ok(response);
    }


    /**
     * 2) 진료 상세 조회
     * GET /api/v1/consultations/{consultationId}
     */
    @GetMapping("/{consultationId}")
    public ResponseEntity<ConsultationDetailResponseDto> getConsultationDetail(
            @PathVariable Long consultationId
    ) {
        ConsultationDetailResponseDto response =
                consultationService.getConsultationDetail(consultationId);

        return ResponseEntity.ok(response);
    }


    /**
     * 3) 내 전체 진료 이력 조회
     * GET /api/v1/consultations/my?patientId=uuid
     */
    @GetMapping("/my")
    public ResponseEntity<List<ConsultationListResponseDto>> getMyConsultations(
            @RequestParam UUID patientId
    ) {
        List<ConsultationListResponseDto> response =
                consultationService.getMyConsultations(patientId);

        return ResponseEntity.ok(response);
    }


    /**
     * 4) 날짜별 진료 조회 (캘린더)
     * GET /api/v1/consultations/my/date?patientId=uuid&date=2025-11-29
     */
    @GetMapping("/my/date")
    public ResponseEntity<List<ConsultationListResponseDto>> getMyConsultationsByDate(
            @RequestParam UUID patientId,
            @RequestParam String date // "YYYY-MM-DD"
    ) {
        LocalDate parsedDate = LocalDate.parse(date);

        List<ConsultationListResponseDto> response =
                consultationService.getConsultationsByDate(patientId, parsedDate);

        return ResponseEntity.ok(response);
    }
}
