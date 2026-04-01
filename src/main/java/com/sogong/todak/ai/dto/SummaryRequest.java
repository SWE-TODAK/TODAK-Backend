package com.sogong.todak.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record SummaryRequest(
        @JsonProperty("recordingid") UUID recordingId,
        @JsonProperty("transcript") String transcript
) {
}