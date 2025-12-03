package com.todak.api.summary.service;

import com.todak.api.summary.dto.request.SummaryCreateRequestDto;
import com.todak.api.summary.dto.response.SummaryResponseDto;

public interface SummaryService {

    SummaryResponseDto createSummary(SummaryCreateRequestDto req);

    SummaryResponseDto getSummaryByConsultation(Long consultationId);
}
