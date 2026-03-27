package com.sogong.todak.ai.dto;

import java.util.UUID;

public record SummaryRequest(
        UUID recordingId,
        String transcript
) {
}