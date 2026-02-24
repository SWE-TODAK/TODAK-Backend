package com.sogong.todak.health.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateMetricRequest {
    private String name;
    private String unit;
}