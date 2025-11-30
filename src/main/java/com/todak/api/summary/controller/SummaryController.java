package com.todak.api.summary.controller;

import com.todak.api.summary.dto.request.SummaryCreateRequestDto;
import com.todak.api.summary.dto.response.SummaryResponseDto;
import com.todak.api.summary.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summaries")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    /**
     * 1. 요약 생성
     * POST /api/summaries
     * body = { consultationId, recordingId }
     */
    @PostMapping
    public ResponseEntity<SummaryResponseDto> createSummary(
            @RequestBody SummaryCreateRequestDto request
    ) {
        SummaryResponseDto response = summaryService.createSummary(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 2. Consultation 기반 요약 조회
     * GET /api/summaries/{consultationId}
     */
    @GetMapping("/{consultationId}")
    public ResponseEntity<SummaryResponseDto> getSummary(
            @PathVariable Long consultationId
    ) {
        SummaryResponseDto response = summaryService.getSummaryByConsultation(consultationId);

        return ResponseEntity.ok(response);
    }
}
