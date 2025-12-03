package com.todak.api.appointment.controller;

import com.todak.api.appointment.dto.request.AppointmentCancelRequest;
import com.todak.api.appointment.dto.request.AppointmentCreateRequestDto;
import com.todak.api.appointment.dto.response.AppointmentResponseDto;
import com.todak.api.appointment.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//test

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
            @AuthenticationPrincipal Long kakaoId,
            @RequestBody AppointmentCreateRequestDto request
    ) {
        return ResponseEntity.ok(appointmentService.create(kakaoId, request));
    }

    /** -----------------------------------------
     *  2. 예약 취소
     *  PATCH /api/appointments/cancel
     * ----------------------------------------- */
    @PatchMapping("/cancel")
    public ResponseEntity<Void> cancelAppointment(
            @AuthenticationPrincipal Long kakaoId,
            @RequestBody AppointmentCancelRequest request
    ) {
        appointmentService.cancel(kakaoId, request);
        return ResponseEntity.ok().build();
    }

    /** -----------------------------------------
     *  3. 나의 전체 예약 목록
     *  GET /api/appointments/my
     * ----------------------------------------- */
    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponseDto>> getMyAppointments(
            @AuthenticationPrincipal Long kakaoId
    ) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(kakaoId));
    }

    /** -----------------------------------------
     *  4. 오늘 나의 예약
     *  GET /api/appointments/my/today
     * ----------------------------------------- */
    @GetMapping("/my/today")
    public ResponseEntity<AppointmentResponseDto> getTodayMyAppointment(
            @AuthenticationPrincipal Long kakaoId
    ) {
        return ResponseEntity.ok(appointmentService.getTodayMyAppointment(kakaoId));
    }

    /** -----------------------------------------
     *  5. 병원 전체 예약 조회
     *  GET /api/appointments/hospitals/{hospitalId}
     * ----------------------------------------- */
    @GetMapping("/hospitals/{hospitalId}")
    public ResponseEntity<List<AppointmentResponseDto>> getHospitalAppointments(
            @AuthenticationPrincipal Long kakaoId,
            @PathVariable Long hospitalId
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByHospital(kakaoId, hospitalId));
    }

    /** -----------------------------------------
     *  6. 병원 특정 날짜 예약 조회
     *  GET /api/appointments/hospitals/{hospitalId}/date?date=YYYY-MM-DD
     * ----------------------------------------- */
    @GetMapping("/hospitals/{hospitalId}/date")
    public ResponseEntity<List<AppointmentResponseDto>> getHospitalAppointmentsByDate(
            @AuthenticationPrincipal Long kakaoId,
            @PathVariable Long hospitalId,
            @RequestParam String date
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByHospitalAndDate(kakaoId, hospitalId, date)
        );
    }
}
