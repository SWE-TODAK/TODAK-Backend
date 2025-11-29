package com.todak.api.consultation.repository;

import com.todak.api.consultation.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    /**
     * 환자의 전체 진료 목록 조회
     */
    List<Consultation> findByPatientId(UUID patientId);

    /**
     * 병원 입장에서 특정 병원의 진료 목록 조회 (관리자 기능 등)
     */
    List<Consultation> findByHospital_HospitalId(Long hospitalId);

    /**
     * 캘린더 기능: 날짜 범위로 조회
     * - dayStart: 2025-01-01T00:00:00
     * - dayEnd:   2025-01-01T23:59:59
     */
    List<Consultation> findByPatientIdAndStartedAtBetween(
            UUID patientId,
            OffsetDateTime start,
            OffsetDateTime end
    );

    /**
     * appointmentId로 조회 (예약 → 진료 연결 목적)
     */
    List<Consultation> findByAppointmentId(Long appointmentId);
}
