package com.sogong.todak.health.dto.request;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class RecordValueRequest {
    private String metricId;

    private String metricType;

    private Double value;
    private Integer systolic;
    private Integer diastolic;
    private LocalDateTime recordedAt;
    private Integer beforeMeal;
    private Integer afterMeal;
    private Double totalChol;
    private Double triglyceride;
    private Double hdl;
    private Double ldl;
}