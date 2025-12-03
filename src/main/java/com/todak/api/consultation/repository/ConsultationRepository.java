package com.todak.api.consultation.repository;

import com.todak.api.consultation.entity.Consultation;
import com.todak.api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    /**
     * 환자의 전체 진료 목록 조회
     */
    List<Consultation> findByPatient(User patient);

    /**
     * 병원 입장에서 특정 병원의 진료 목록 조회 (관리자 기능)
     */
    List<Consultation> findByHospital_HospitalId(Long hospitalId);

    /**
     * 캘린더 기능: 날짜 범위로 조회
     */
    List<Consultation> findByPatientAndStartedAtBetween(
            User patient,
            OffsetDateTime start,
            OffsetDateTime end
    );

    /**
     * 예약 ID로 조회 (예약 → 진료 연결 목적)
     */
    List<Consultation> findByAppointmentId(Long appointmentId);
}
