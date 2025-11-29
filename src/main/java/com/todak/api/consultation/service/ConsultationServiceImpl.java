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

    // -------------------------
    // 1) 진료 생성
    // -------------------------
    @Override
    public ConsultationCreateResponseDto startConsultation(Long appointmentId, UUID patientId) {

        // appointment → hospital 매핑은 나중에 구현
        // 일단 임시 병원 1로 가정하거나 컨트롤러에서 넘겨주도록 할 수 있음
        Hospital hospital = hospitalRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found"));

        // 예약 시간도 일단 now로 (나중엔 Appointment.startTime 참조)
        Consultation consultation = Consultation.builder()
                .appointmentId(appointmentId)
                .hospital(hospital)
                .patientId(patientId)
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

    // -------------------------
    // 2) 진료 상세 조회
    // -------------------------
    @Override
    public ConsultationDetailResponseDto getConsultationDetail(Long consultationId) {

        Consultation c = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("Consultation not found"));

        // Summary (latest)
        Summary summary = summaryRepository.findTopByConsultationIdOrderByCreatedAtDesc(consultationId)
                .orElse(null);

        // Recording (latest)
        Recording recording = recordingRepository.findByConsultationId(consultationId)
                .orElse(null);

        return ConsultationDetailResponseDto.builder()
                .consultationId(c.getConsultationId())
                .hospitalName(c.getHospital().getName())
                .doctorName(null) // 의사 기능 나중에 붙이면 가능
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

    // -------------------------
    // 3) 전체 진료 이력 조회
    // -------------------------
    @Override
    public List<ConsultationListResponseDto> getMyConsultations(UUID patientId) {

        return consultationRepository.findByPatientId(patientId)
                .stream()
                .map(c -> {

                    Summary summary = summaryRepository.findTopByConsultationIdOrderByCreatedAtDesc(
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

    // -------------------------
    // 4) 캘린더 날짜 조회
    // -------------------------
    @Override
    public List<ConsultationListResponseDto> getConsultationsByDate(UUID patientId, LocalDate date) {

        OffsetDateTime start = date.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = date.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        return consultationRepository.findByPatientIdAndStartedAtBetween(patientId, start, end)
                .stream()
                .map(c -> {

                    Summary summary = summaryRepository.findTopByConsultationIdOrderByCreatedAtDesc(
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
