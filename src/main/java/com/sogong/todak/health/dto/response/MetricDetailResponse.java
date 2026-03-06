package com.sogong.todak.health.dto.response;

import lombok.*;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricDetailResponse {
    private String metricValueId;
    private String metricType;
    private String recordedAt;
    private String unit;
    private List<ValueLabelDto> values;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValueLabelDto { // public으로 선언하여 서비스에서 접근 가능하게 함
        private String label;
        private Object value;
    }
}