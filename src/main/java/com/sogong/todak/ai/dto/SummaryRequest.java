package com.sogong.todak.ai.dto;

import java.util.UUID;

public record SummaryRequest(
        String consultationId,
        UUID recordingId,
        String transcript
) {
}