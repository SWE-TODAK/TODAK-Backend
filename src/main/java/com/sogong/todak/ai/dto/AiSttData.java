package com.sogong.todak.ai.dto;

import java.util.Map;
import java.util.UUID;

public record AiSttData(
        UUID recordingId,
        String language,
        String transcript,
        Map<String, Object> meta
) {
}