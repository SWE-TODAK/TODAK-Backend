package com.sogong.todak.health.dto.request;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class RecordValueRequest {
    private String metricId;
    private Double value;
    private Integer systolic;
    private Integer diastolic;
    private LocalDateTime recordedAt;
}