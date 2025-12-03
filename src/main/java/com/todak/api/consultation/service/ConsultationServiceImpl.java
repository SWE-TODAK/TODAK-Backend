package com.todak.api.consultation.service;

import com.todak.api.consultation.dto.response.*;
import com.todak.api.consultation.dto.request.*;
import com.todak.api.consultation.entity.Consultation;
import com.todak.api.consultation.repository.ConsultationRepository;
import com.todak.api.recording.entity.Recording;
import com.todak.api.recording.repository.RecordingRepository;
import com.todak.api.summary.entity.Summary;
import com.todak.api.summary.repository.SummaryRepository;
import com.todak.api.hospital.entity.Hospital;
import com.todak.api.hospital.repository.HospitalRepository;
import com.todak.api.user.entity.User;
import com.todak.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final RecordingRepository recordingRepository;
    private final SummaryRepository summaryRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;

    // 1) 진료 생성
    @Override
    public ConsultationCreateResponseDto startConsultation(Long appointmentId, Long kakaoId) {

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Hospital hospital = hospitalRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found"));

        Consultation consultation = Consultation.builder()
                .appointmentId(appointmentId)
                .hospital(hospital)
                .patient(user)   // 🔥 UUID 대신 User 엔티티 통째로 저장
                .startedAt(OffsetDateTime.now())
                .build();

        consultationRepository.save(consultation);

        return ConsultationCreateResponseDto.builder()
                .consultationId(consultation.getConsultationId())
                .appointmentId(appointmentId)
                .hospitalName(hospital.getName())
                .consultationTime(consultation.getStartedAt())
                .build();
    }

    // 2) 진료 상세 조회
    @Override
    public ConsultationDetailResponseDto getConsultationDetail(Long consultationId) {

        Consultation c = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("Consultation not found"));

        Summary summary = summaryRepository.findByConsultationId(consultationId).orElse(null);

        Recording recording = recordingRepository
                .findFirstByConsultation_ConsultationId(consultationId)
                .orElse(null);

        return ConsultationDetailResponseDto.builder()
                .consultationId(c.getConsultationId())
                .hospitalName(c.getHospital().getName())
                .doctorName(null)
                .consultationTime(c.getStartedAt())

                .summary(summary == null ? null :
                        ConsultationDetailResponseDto.SummaryBlock.builder()
                                .summaryId(summary.getSummaryId())
                                .content(summary.getContent())
                                .tags(summary.getTags())
                                .build())

                .recording(recording == null ? null :
                        ConsultationDetailResponseDto.RecordingBlock.builder()
                                .recordingId(recording.getRecordingId())
                                .fileUrl(recording.getFilePath())
                                .duration(recording.getDurationSeconds())
                                .status(recording.getStatus().name())
                                .build())
                .build();
    }

    // 3) 나의 전체 진료 이력
    @Override
    public List<ConsultationListResponseDto> getMyConsultations(Long kakaoId) {

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return consultationRepository.findByPatient(user)
                .stream()
                .map(c -> {

                    Summary summary = summaryRepository.findByConsultationId(
                            c.getConsultationId()).orElse(null);

                    return ConsultationListResponseDto.builder()
                            .consultationId(c.getConsultationId())
                            .hospitalName(c.getHospital().getName())
                            .doctorName(null)
                            .consultationTime(c.getStartedAt())
                            .summaryPreview(summary == null ? null : summary.getContent())
                            .build();
                })
                .toList();
    }

    // 4) 캘린더 날짜 조회
    @Override
    public List<ConsultationListResponseDto> getConsultationsByDate(Long kakaoId, LocalDate date) {

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        OffsetDateTime start = date.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = date.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        return consultationRepository.findByPatientAndStartedAtBetween(user, start, end)
                .stream()
                .map(c -> {

                    Summary summary = summaryRepository.findByConsultationId(
                            c.getConsultationId()).orElse(null);

                    return ConsultationListResponseDto.builder()
                            .consultationId(c.getConsultationId())
                            .hospitalName(c.getHospital().getName())
                            .doctorName(null)
                            .consultationTime(c.getStartedAt())
                            .summaryPreview(summary == null ? null : summary.getContent())
                            .build();
                })
                .toList();
    }
}
