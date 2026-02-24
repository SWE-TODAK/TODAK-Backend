package com.sogong.todak.health.controller;

import com.sogong.todak.health.dto.request.RecordValueRequest;
import com.sogong.todak.health.service.HealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // 수치 추가 (Batch)
    @PostMapping("/metrics/batch")
    public ResponseEntity<?> recordMetricValue(@RequestBody RecordValueRequest request) {
        // service.recordValue(request) 구현 필요
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "수치가 저장되었습니다.",
                "data", Map.of("metricValueId", "v-generated-id")
        ));
    }
}