package com.sogong.todak.health.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class MetricQueryResponse {
    private String metricType; // 예: BLOOD_PRESSURE
    private List<HistoryDto> history;

    @Getter
    @Builder
    public static class HistoryDto {
        private UUID metricValueId;
        private String metricId;
        private String date;
        private Integer systolic;
        private Integer diastolic;
        private Double value;
        private String status;
    }
}