package com.todak.api.recording.dto.response;

import com.todak.api.recording.entity.Recording;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecordingDetailResponseDto {

    private Long recordingId;
    private Long consultationId;
    private Long hospitalId;

    private String filePath;
    private Integer durationSeconds;
    private Double fileSizeMb;

    private String transcript;
    private String status;

    private String createdAt;
    private String authorizedAt;

    public static RecordingDetailResponseDto from(Recording r) {
        return RecordingDetailResponseDto.builder()
                .recordingId(r.getRecordingId())
                .consultationId(r.getConsultation().getConsultationId())
                .hospitalId(r.getConsultation().getHospital().getHospitalId())
                .filePath(r.getFilePath())
                .durationSeconds(r.getDurationSeconds())
                .fileSizeMb(r.getFileSizeMb())
                .transcript(r.getTranscript())
                .status(r.getStatus().name())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : null)
                .authorizedAt(r.getAuthorizedAt() != null ? r.getAuthorizedAt().toString() : null)
                .build();
    }
}
