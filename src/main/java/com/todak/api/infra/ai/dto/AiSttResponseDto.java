package com.todak.api.infra.ai.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiSttResponseDto {

    private int status;
    private String message;
    private SttData data;

    @Getter
    @Setter
    public static class SttData {
        private String recordingId;
        private String consultationId;

        private Integer duration;
        private String language;
        private String transcript;

        private Meta meta;
    }

    @Getter
    @Setter
    public static class Meta {
        private String provider;
        private String model;
        private String processedAt;
    }
}
