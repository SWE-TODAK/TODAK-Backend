package com.sogong.todak.ai.dto;

import java.util.Map;
import java.util.UUID;

public record AiSttData(
        UUID recordingId,
        UUID consultationId,
        Integer duration,
        String language,
        String transcript,
        Map<String, Object> meta
) {
}