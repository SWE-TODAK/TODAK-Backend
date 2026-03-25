package com.sogong.todak.recording.dto.request;

import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class UpdateRecordingMetadataRequest {
    private String hospitalName;
    private String diseaseName;
    private String doctorName;
    private String departmentName;
    private OffsetDateTime consultedAt;
    private String title;
}