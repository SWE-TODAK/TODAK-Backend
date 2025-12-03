package com.todak.api.appointment.service;

import com.todak.api.appointment.dto.request.AppointmentCreateRequestDto;
import com.todak.api.appointment.dto.request.AppointmentCancelRequest;
import com.todak.api.appointment.dto.response.AppointmentResponseDto;

import java.util.List;

public interface AppointmentService {

    // 1. 예약 생성
    AppointmentResponseDto create(Long kakaoId, AppointmentCreateRequestDto request);

    // 2. 예약 취소
    void cancel(Long kakaoId, AppointmentCancelRequest request);

    // 3. 나의 모든 예약 조회
    List<AppointmentResponseDto> getMyAppointments(Long kakaoId);

    // 4. 오늘 나의 예약 조회
    AppointmentResponseDto getTodayMyAppointment(Long kakaoId);

    // 5. 병원 전체 예약 조회
    List<AppointmentResponseDto> getAppointmentsByHospital(Long kakaoId, Long hospitalId);

    // 6. 병원 특정 날짜 예약 조회
    List<AppointmentResponseDto> getAppointmentsByHospitalAndDate(Long kakaoId, Long hospitalId, String date);
}
