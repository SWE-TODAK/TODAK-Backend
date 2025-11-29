package com.todak.api.summary.controller;

import com.todak.api.summary.dto.response.SummaryResponseDto;
import com.todak.api.summary.dto.request.SummaryCreateRequestDto;
import com.todak.api.summary.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    /**
     * 요약 생성
     * content & tags는 AI 서버에서 받아온 결과를 기반으로 생성할 것
     */
    @PostMapping("/{consultationId}")
    public ResponseEntity<SummaryResponseDto> createSummary(
            @PathVariable Long consultationId,
            @RequestParam(required = false) Long recordingId,
            @RequestBody SummaryCreateRequestDto request
    ) {
        return ResponseEntity.ok(
                summaryService.createSummary(
                        consultationId,
                        recordingId,
                        request.getContent(),
                        request.getTags()
                )
        );
    }

    /**
     * 특정 진료의 요약 조회
     */
    @GetMapping("/{consultationId}")
    public ResponseEntity<SummaryResponseDto> getSummaryByConsultation(
            @PathVariable Long consultationId
    ) {
        return ResponseEntity.ok(
                summaryService.getSummaryByConsultation(consultationId)
        );
    }
}
