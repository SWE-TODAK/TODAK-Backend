package com.todak.api.summary.controller;

import com.todak.api.summary.dto.response.SummaryResponseDto;
import com.todak.api.summary.dto.request.SummaryCreateRequestDto;
import com.todak.api.summary.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api.todak.app/v1/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    /**
     * 1) 요약 생성 (AI가 준 content, tags 기반)
     * POST /api/v1/summary/{consultationId}?recordingId=10
     */
    @PostMapping("/{consultationId}")
    public ResponseEntity<SummaryResponseDto> createSummary(
            @PathVariable Long consultationId,
            @RequestParam(required = false) Long recordingId,
            @RequestBody SummaryCreateRequestDto request
    ) {
        SummaryResponseDto response = summaryService.createSummary(
                consultationId,
                recordingId,
                request.getContent(),
                request.getTags()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 2) 특정 진료의 최신 요약 조회
     * GET /api/v1/summary/{consultationId}
     */
    @GetMapping("/{consultationId}")
    public ResponseEntity<SummaryResponseDto> getLatestSummaryByConsultation(
            @PathVariable Long consultationId
    ) {
        SummaryResponseDto response =
                summaryService.getLatestByConsultation(consultationId);

        return ResponseEntity.ok(response);
    }
}
