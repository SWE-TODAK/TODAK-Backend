package com.sogong.todak.recording.dto.response;

import com.sogong.todak.recording.entity.Recording;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class RecordingDetailResponse {
    private UUID recordingId;
    private String title;
    private String memo;
    private Integer durationMs;
    private OffsetDateTime createdAt;
    private String status;

    private String hospitalName;
    private String diseaseName;
    private String doctorName;
    private String departmentName;
    private OffsetDateTime consultedAt;

    public static RecordingDetailResponse from(Recording recording) {
        return RecordingDetailResponse.builder()
                .recordingId(recording.getRecordingId())
                .title(recording.getTitle())
                .memo(recording.getMemo())
                .durationMs(recording.getDurationMs())
                .createdAt(recording.getCreatedAt())
                .status(recording.getStatus().name())
                .hospitalName(recording.getHospitalName())
                .diseaseName(recording.getDiseaseName())
                .doctorName(recording.getDoctorName())
                .departmentName(recording.getDepartmentName())
                .consultedAt(recording.getConsultedAt())
                .build();
    }
}