package com.todak.api.consultation.entity;

import com.todak.api.hospital.entity.Hospital;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "consultations")
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consultation_id")
    private Long consultationId;

    /**
     * 예약 ID (진료는 예약을 기반으로 생성됨)
     * Appointment 테이블이 생기면 ManyToOne으로 변경 가능
     */
    @Column(name = "appointment_id")
    private Long appointmentId;

    /**
     * 병원 FK
     * appointments.hospital_id와 동일
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    /**
     * 환자 UUID (users 테이블의 PK)
     */
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    /**
     * 진료 시작 시간 = 예약 시간 (조회 시 캘린더/리스트에서 쓰임)
     */
    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    /**
     * 진료 기록 생성 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
