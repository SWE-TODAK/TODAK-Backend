package com.todak.api.summary.service;

import com.todak.api.summary.dto.response.SummaryResponseDto;
import com.todak.api.summary.entity.Summary;

public interface SummaryService {

    // 요약 생성
    SummaryResponseDto createSummary(Long consultationId,
                                     Long recordingId,
                                     String content,
                                     String tags);

    // 진료 기준 최신 요약 조회
    SummaryResponseDto getLatestByConsultation(Long consultationId);

    // 녹음 기준 최신 요약 조회
    SummaryResponseDto getLatestByRecording(Long recordingId);
}
