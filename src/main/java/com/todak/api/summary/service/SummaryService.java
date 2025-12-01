package com.todak.api.summary.service;

import com.todak.api.summary.dto.request.SummaryCreateRequestDto;
import com.todak.api.summary.dto.response.SummaryResponseDto;
import com.todak.api.summary.entity.Summary;

public interface SummaryService {

    SummaryResponseDto createSummary(SummaryCreateRequestDto req);

    // 진료 기준 최신 요약 조회
    SummaryResponseDto getLatestByConsultation(Long consultationId);

    // 녹음 기준 최신 요약 조회
    SummaryResponseDto getLatestByRecording(Long recordingId);
}
