package com.todak.api.consultation.service;

import com.todak.api.consultation.dto.request.ConsultationCreateResponseDto;
import com.todak.api.consultation.dto.response.ConsultationDetailResponseDto;
import com.todak.api.consultation.dto.response.ConsultationListResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ConsultationService {

    // 진료 생성 (예약시간을 기반으로)
    ConsultationCreateResponseDto startConsultation(Long appointmentId, UUID patientId);

    // 진료 상세 조회
    ConsultationDetailResponseDto getConsultationDetail(Long consultationId);

    // 나의 전체 진료 이력 조회
    List<ConsultationListResponseDto> getMyConsultations(UUID patientId);

    // 캘린더 날짜별 진료 조회
    List<ConsultationListResponseDto> getConsultationsByDate(UUID patientId, LocalDate date);
}
