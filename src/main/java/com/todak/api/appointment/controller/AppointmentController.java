package com.todak.api.appointment.controller;

import com.todak.api.appointment.dto.request.AppointmentCancelRequest;
import com.todak.api.appointment.dto.request.AppointmentCreateRequestDto;
import com.todak.api.appointment.dto.response.AppointmentResponseDto;
import com.todak.api.appointment.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    /** -----------------------------------------
     *  1. 예약 생성
     *  POST /api/appointments
     * ----------------------------------------- */
    @PostMapping
    public ResponseEntity<AppointmentResponseDto> createAppointment(
            @RequestBody AppointmentCreateRequestDto request
    ) {
        return ResponseEntity.ok(appointmentService.create(request));
    }

    /** -----------------------------------------
     *  2. 예약 취소
     *  PATCH /api/appointments/cancel
     * ----------------------------------------- */
    @PatchMapping("/cancel")
    public ResponseEntity<Void> cancelAppointment(
            @RequestBody AppointmentCancelRequest request
    ) {
        appointmentService.cancel(request);
        return ResponseEntity.ok().build();
    }

    /** -----------------------------------------
     *  3. 나의 전체 예약 목록
     *  GET /api/appointments/my/{patientId}
     * ----------------------------------------- */
    @GetMapping("/my/{patientId}")
    public ResponseEntity<List<AppointmentResponseDto>> getMyAppointments(
            @PathVariable String patientId
    ) {
        UUID uuid = UUID.fromString(patientId);
        return ResponseEntity.ok(appointmentService.getMyAppointments(uuid));
    }

    /** -----------------------------------------
     *  4. 오늘 나의 예약
     *  GET /api/appointments/my/{patientId}/today
     * ----------------------------------------- */
    @GetMapping("/my/{patientId}/today")
    public ResponseEntity<AppointmentResponseDto> getTodayMyAppointment(
            @PathVariable String patientId
    ) {
        UUID uuid = UUID.fromString(patientId);
        return ResponseEntity.ok(appointmentService.getTodayMyAppointment(uuid));
    }

    /** -----------------------------------------
     *  5. 병원 전체 예약 조회
     *  GET /api/appointments/hospitals/{hospitalId}
     * ----------------------------------------- */
    @GetMapping("/hospitals/{hospitalId}")
    public ResponseEntity<List<AppointmentResponseDto>> getHospitalAppointments(
            @PathVariable Long hospitalId
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByHospital(hospitalId));
    }

    /** -----------------------------------------
     *  6. 병원 특정 날짜 예약 조회
     *  GET /api/appointments/hospitals/{hospitalId}/date?date=2025-11-30
     * ----------------------------------------- */
    @GetMapping("/hospitals/{hospitalId}/date")
    public ResponseEntity<List<AppointmentResponseDto>> getHospitalAppointmentsByDate(
            @PathVariable Long hospitalId,
            @RequestParam String date
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByHospitalAndDate(hospitalId, date)
        );
    }
}
