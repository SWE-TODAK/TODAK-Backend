package com.todak.api.appointment.dto.response;

import com.todak.api.appointment.entity.Appointment;
import com.todak.api.appointment.entity.AppointmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class AppointmentResponseDto {

    private Long appointmentId;
    private UUID patientId;

    private Long hospitalId;
    private String hospitalName;

    private Long doctorId;
    private String doctorName;

    private Long departmentId;
    private String departmentName;

    private OffsetDateTime datetime;
    private AppointmentStatus status;

    public static AppointmentResponseDto from(Appointment a) {

        return AppointmentResponseDto.builder()
                .appointmentId(a.getAppointmentId())
                .patientId(a.getPatient().getUserUuid())

                .hospitalId(a.getHospital().getHospitalId())
                .hospitalName(a.getHospital().getName())

                .doctorId(a.getDoctor() != null ? a.getDoctor().getDoctorId() : null)
                .doctorName(a.getDoctor() != null ? a.getDoctor().getName() : null)

                .departmentId(a.getDepartment() != null ? a.getDepartment().getDepartmentId() : null)
                .departmentName(a.getDepartment() != null ? a.getDepartment().getName() : null)

                .datetime(a.getDatetime())
                .status(a.getStatus())
                .build();
    }
}
