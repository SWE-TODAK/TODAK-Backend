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
    private String transcript;  // STT 원문
    private String status;

    public static RecordingDetailResponseDto from(Recording r) {
        return RecordingDetailResponseDto.builder()
                .recordingId(r.getRecordingId())
                .consultationId(r.getConsultationId())
                .hospitalId(r.getHospital().getHospitalId())
                .filePath(r.getFilePath())
                .durationSeconds(r.getDurationSeconds())
                .fileSizeMb(r.getFileSizeMb())
                .transcript(r.getTranscript())
                .status(r.getStatus().name())
                .build();
    }
}
