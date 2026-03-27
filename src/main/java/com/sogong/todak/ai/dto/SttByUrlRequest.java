package com.sogong.todak.ai.dto;

import java.util.UUID;

public record SttByUrlRequest(
        UUID recordingId,
        String language,
        String audioUrl,
        Boolean vadEnabled,
        Integer maxSegmentSec,
        Integer vadAggressiveness,
        Integer vadPadMs,
        Double vadMinSegmentSec
) {
}