package com.todak.api.summary.service;

import com.todak.api.summary.dto.response.SummaryResponseDto;

public interface SummaryService {

    SummaryResponseDto createSummary(Long consultationId,
                                     Long recordingId,
                                     String content,
                                     String tags);

    SummaryResponseDto getSummaryByConsultation(Long consultationId);
}
