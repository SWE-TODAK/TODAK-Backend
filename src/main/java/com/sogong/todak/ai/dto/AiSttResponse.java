package com.sogong.todak.ai.dto;

import java.util.Map;

public record AiSttResponse(
        Integer status,
        String message,
        AiSttData data
) {
}