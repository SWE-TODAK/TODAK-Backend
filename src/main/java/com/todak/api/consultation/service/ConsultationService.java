package com.todak.api.consultation.service;

import com.todak.api.consultation.dto.request.ConsultationCreateResponseDto;
import com.todak.api.consultation.dto.response.ConsultationDetailResponseDto;
import com.todak.api.consultation.dto.response.ConsultationListResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ConsultationService {

    ConsultationCreateResponseDto startConsultation(Long appointmentId, Long kakaoId);

    ConsultationDetailResponseDto getConsultationDetail(Long consultationId);

    List<ConsultationListResponseDto> getMyConsultations(Long kakaoId);

    List<ConsultationListResponseDto> getConsultationsByDate(Long kakaoId, LocalDate date);
}

