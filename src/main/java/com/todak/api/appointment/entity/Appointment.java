package com.todak.api.appointment.entity;

import com.todak.api.hospital.entity.Hospital;
import com.todak.api.hospital.entity.Department;
import com.todak.api.hospital.entity.Doctor;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "appointments")
public class Appointment {

    /** appointment_id (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    /** patient_id (FK → users, UUID) */
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    /** hospital_id (FK → hospitals) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    /** datetime : 예약 시간 */
    @Column(name = "datetime", nullable = false)
    private OffsetDateTime datetime;

    /** status : 예약 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status;

    /**
     * doctor_id (FK → doctors)
     * 의사 선택 시에만 값 있음 (NULL 허용)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    /**
     * department_id (FK → departments)
     * 진료과 선택만 한 경우 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
}
