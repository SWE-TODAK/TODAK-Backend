package com.sogong.todak.health.controller;

import com.sogong.todak.health.dto.request.RecordValueRequest;
import com.sogong.todak.health.service.HealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.sogong.todak.health.dto.request.CreateMetricRequest;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {
    private final HealthService healthService;

    // 그래프 데이터 조회
    @GetMapping("/metrics/{metricId}/query")
    public ResponseEntity<?> getMetricQuery(
            @PathVariable UUID metricId,
            @RequestParam(defaultValue = "5") int limit) {
        var data = healthService.getMetricHistory(metricId, limit);
        return ResponseEntity.ok(Map.of("status", 200, "data", data));
    }

    // 상세 정보 조회
    @GetMapping("/metrics/values/{metricValueId}")
    public ResponseEntity<?> getMetricValueDetail(@PathVariable UUID metricValueId) {
        var data = healthService.getMetricDetail(metricValueId);
        return ResponseEntity.ok(Map.of("status", 200, "data", data));
    }

    @PostMapping("/metrics/batch")
    public ResponseEntity<?> recordMetricValue(
            @AuthenticationPrincipal UUID userId, // SecurityContext 등 프로젝트 설정에 맞춰 변환 필요할 수 있음
            @RequestBody RecordValueRequest request) {

        UUID metricValueId = healthService.recordValue(userId, request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "건강 수치가 성공적으로 저장되었습니다.",
                "data", Map.of("metricValueId", metricValueId)
        ));
    }

    // 2. 나의 건강지표 목록 조회
    @GetMapping("/metrics")
    public ResponseEntity<?> getMyMetrics(@AuthenticationPrincipal UUID userId) {
        var data = healthService.getMyMetrics(userId);
        return ResponseEntity.ok(Map.of("status", 200, "data", data));
    }

    // 3. 나만의 건강지표 수동 생성
    @PostMapping("/metrics")
    public ResponseEntity<?> createCustomMetric(
            @AuthenticationPrincipal UUID userId,
            @RequestBody CreateMetricRequest request) {

        UUID metricId = healthService.createCustomMetric(userId, request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "새로운 건강 지표가 생성되었습니다.",
                "data", Map.of("metricId", metricId)
        ));
    }

    // 4. 건강 지표 삭제
    @DeleteMapping("/metrics/{metricId}")
    public ResponseEntity<?> deleteMetric(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID metricId) {

        healthService.deleteMetric(userId, metricId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "건강 지표와 관련된 모든 기록이 삭제되었습니다."
        ));
    }

    // 5. 건강 수치 삭제
    @DeleteMapping("/metrics/values/{metricValueId}")
    public ResponseEntity<?> deleteMetricValue(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID metricValueId) {

        healthService.deleteMetricValue(userId, metricValueId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "해당 건강 수치 기록이 삭제되었습니다."
        ));
    }
}