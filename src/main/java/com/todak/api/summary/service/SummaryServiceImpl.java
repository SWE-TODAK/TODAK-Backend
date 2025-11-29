// src/main/java/com/todak/api/summary/service/SummaryServiceImpl.java
package com.todak.api.summary.service;

import com.todak.api.summary.dto.response.SummaryResponseDto;
import com.todak.api.summary.entity.Summary;
import com.todak.api.summary.repository.SummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

    private final SummaryRepository summaryRepository;

    @Override
    public SummaryResponseDto createSummary(Long consultationId,
                                            Long recordingId,
                                            String content,
                                            String tags) {

        Summary summary = Summary.builder()
                .consultationId(consultationId)
                .recordingId(recordingId)
                .content(content)
                .tags(tags)
                .build();

        summaryRepository.save(summary);

        return SummaryResponseDto.from(summary);
    }

    @Override
    public SummaryResponseDto getSummaryByConsultation(Long consultationId) {

        Summary summary = summaryRepository.findByConsultationId(consultationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Summary not found for consultationId=" + consultationId)
                );

        return SummaryResponseDto.from(summary);
    }
}
