package com.sogong.todak.health.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HealthMetricResponse {
    private String metricId;
    private String name;
    private boolean isCustom;

    private String unit;
    private String metricType;
}