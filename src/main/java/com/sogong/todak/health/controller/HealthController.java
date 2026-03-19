package com.sogong.todak.health.controller;

import com.sogong.todak.health.dto.request.CreateMetricRequest;
import com.sogong.todak.health.dto.request.RecordValueRequest;
import com.sogong.todak.health.service.HealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    // 1. 나의 건강지표 목록 조회 (GET)
    @GetMapping("/metrics")
    public ResponseEntity<?> getMyMetrics(@AuthenticationPrincipal UUID userId) {
        var data = healthService.getMyMetrics(userId);
        return ResponseEntity.ok(Map.of("status", 200, "data", data));
    }

    // 2. 나만의 건강지표 수동 생성 (POST)
    @PostMapping("/metrics")
    public ResponseEntity<?> createCustomMetric(
            @AuthenticationPrincipal UUID userId,
            @RequestBody CreateMetricRequest request) {

        UUID metricId = healthService.createCustomMetric(userId, request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "data", Map.of("metricId", metricId)
        ));
    }

    // 3. 건강 수치 추이 조회 (GET)
    @GetMapping("/metrics/{metricId}/query")
    public ResponseEntity<?> getMetricQuery(
            @PathVariable UUID metricId,
            @RequestParam(defaultValue = "5") int limit) {
        var data = healthService.getMetricHistory(metricId, limit);
        return ResponseEntity.ok(Map.of("status", 200, "data", data));
    }

    // 4. 건강 수치 구체적 조회 (GET)
    @GetMapping("/metrics/values/{metricValueId}")
    public ResponseEntity<?> getMetricValueDetail(@PathVariable UUID metricValueId) {
        var data = healthService.getMetricDetail(metricValueId);
        return ResponseEntity.ok(Map.of("status", 200, "data", data));
    }

    // 5. 건강 수치 추가 (POST) - 명세서 응답 구조 완벽 반영
    @PostMapping("/metrics/batch")
    public ResponseEntity<?> recordMetricValue(
            @AuthenticationPrincipal UUID userId,
            @RequestBody RecordValueRequest request) {

        UUID metricValueId = healthService.recordValue(userId, request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "수치가 저장되었습니다.",
                "data", Map.of(
                        "metricValueId", metricValueId,
                        "metricType", request.getMetricType() != null ? request.getMetricType() : "CUSTOM",
                        "value", request.getValue() != null ? request.getValue() : 0.0,
                        "recordedAt", request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now()
                )
        ));
    }

    // 6. 건강 지표 삭제 (DELETE)
    @DeleteMapping("/metrics/{metricId}")
    public ResponseEntity<?> deleteMetric(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID metricId) {

        healthService.deleteMetric(userId, metricId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "기록이 삭제되었습니다.",
                "data", Map.of("metricId", metricId)
        ));
    }

    // 7. 건강 수치 삭제 (DELETE)
    @DeleteMapping("/metrics/values/{metricValueId}")
    public ResponseEntity<?> deleteMetricValue(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID metricValueId) {

        healthService.deleteMetricValue(userId, metricValueId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "해당 측정 기록이 삭제되었습니다.",
                "data", Map.of("metricValueId", metricValueId)
        ));
    }
}