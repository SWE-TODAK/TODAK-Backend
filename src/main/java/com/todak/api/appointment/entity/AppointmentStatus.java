// src/main/java/com/todak/api/appointment/entity/AppointmentStatus.java
package com.todak.api.appointment.entity;

public enum AppointmentStatus {
    REQUESTED,   // 예약 요청함
    APPROVED,    // 병원이 승인함
    CANCELLED,   // 취소됨
    DONE         // 진료 완료
}
