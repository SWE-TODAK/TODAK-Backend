package com.sogong.todak.health.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class MetricQueryResponse {
    private String metricType;
    private String summaryMessage;
    private List<HistoryDto> history;

    @Getter
    @Builder
    public static class HistoryDto {
        private UUID metricValueId;
        private String metricId;
        private String date;
        private Integer systolic;
        private Integer diastolic;
        private Integer beforeMeal;
        private Integer afterMeal;
        private Double totalChol;
        private Double hdl;
        private Double ldl;
        private Double triglyceride;
        private Double value;
        private String status;
    }
}