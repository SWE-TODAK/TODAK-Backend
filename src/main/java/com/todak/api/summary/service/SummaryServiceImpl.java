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

    // -----------------------------
    // 1) 요약 생성 (AI가 준 content+tags를 그대로 저장)
    // -----------------------------
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

    // -----------------------------
    // 2) 진료 기준 최신 Summary 조회
    // -----------------------------
    @Override
    public SummaryResponseDto getLatestByConsultation(Long consultationId) {

        Summary summary = summaryRepository
                .findTopByConsultationIdOrderByCreatedAtDesc(consultationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Summary not found for consultationId: " + consultationId));

        return SummaryResponseDto.from(summary);
    }

    // -----------------------------
    // 3) 녹음 기준 최신 Summary 조회
    // -----------------------------
    @Override
    public SummaryResponseDto getLatestByRecording(Long recordingId) {

        Summary summary = summaryRepository
                .findTopByRecordingIdOrderByCreatedAtDesc(recordingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Summary not found for recordingId: " + recordingId));

        return SummaryResponseDto.from(summary);
    }
}
