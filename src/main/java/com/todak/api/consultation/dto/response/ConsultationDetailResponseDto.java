package com.todak.api.consultation.dto.response;

import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationDetailResponseDto {

    private Long consultationId;

    private String hospitalName;
    private String doctorName;

    private OffsetDateTime consultationTime;

    private SummaryBlock summary;
    private RecordingBlock recording;

    // ---------------------

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SummaryBlock {
        private Long summaryId;
        private String content;
        private String tags;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecordingBlock {
        private Long recordingId;
        private String fileUrl;
        private Integer duration;
        private String status;   // WAITING / UPLOADED / TRANSCRIBED ...
    }
}

