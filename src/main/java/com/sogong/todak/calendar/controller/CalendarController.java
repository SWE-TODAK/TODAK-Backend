package com.sogong.todak.calendar.controller;

import com.sogong.todak.calendar.dto.response.CalendarDetailResponse;
import com.sogong.todak.calendar.dto.response.CalendarMarkResponse;
import com.sogong.todak.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    private UUID extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        Object principal = authentication.getPrincipal();

        // 1. 토큰 해석본이 직접 UUID로 들어오는 경우
        if (principal instanceof UUID) {
            return (UUID) principal;
        }

        // 2. 토큰 해석본이 String(이름) 형태로 들어오는 경우
        try {
            return UUID.fromString(authentication.getName());
        } catch (Exception e) {
            throw new IllegalArgumentException("토큰에서 유저 ID를 추출할 수 없습니다.");
        }
    }

    // 1. 캘린더 마킹 조회 API
    @GetMapping("/recordings/marks")
    public ResponseEntity<?> getCalendarMarks(
            @RequestParam("month") String month,
            Authentication authentication) {

        UUID userId = extractUserId(authentication);

        CalendarMarkResponse data = calendarService.getCalendarMarks(userId, month);

        return ResponseEntity.ok(Map.of("status", 200, "message", "캘린더 마킹 정보 조회 성공", "data", data));
    }

    @GetMapping("/recordings")
    public ResponseEntity<?> getCalendarDetails(
            @RequestParam("date") String date,
    Authentication authentication) {

        UUID userId = extractUserId(authentication);

        // 📝 [미션 4] CalendarService를 호출해서 상세 정보 리스트를 가져오세요!
        List<CalendarDetailResponse> data = calendarService.getCalendarDetails(userId,date);

        return ResponseEntity.ok(Map.of("status", 200, "message", "세부 일자 진료 정보 조회 성공", "data", data));
    }
}