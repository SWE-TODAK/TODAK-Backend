package com.sogong.todak.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record SttByUrlRequest(
        @JsonProperty("recordingId") UUID recordingId,
        @JsonProperty("language") String language,
        @JsonProperty("audioUrl") String audioUrl,
        @JsonProperty("vadEnabled") Boolean vadEnabled,
        @JsonProperty("maxSegmentSec") Integer maxSegmentSec,
        @JsonProperty("vadAggressiveness") Integer vadAggressiveness,
        @JsonProperty("vadPadMs") Integer vadPadMs,
        @JsonProperty("vadMinSegmentSec") Double vadMinSegmentSec
) {
}