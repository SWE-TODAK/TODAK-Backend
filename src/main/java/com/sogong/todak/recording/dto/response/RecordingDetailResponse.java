package com.sogong.todak.recording.dto.response;

import com.sogong.todak.recording.entity.Recording;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Getter
@Builder
public class RecordingDetailResponse {
    private UUID recordingId;
    private String date;
    private String hospitalName;
    private String visitTime;
    private String department;
    private String doctorName;
    private String diagnosisName;
    private String summary;
    private String fullTranscription;
    private String audioUrl;
    private String memo;

    public static RecordingDetailResponse from(Recording r) {
        OffsetDateTime targetDate = r.getConsultedAt() != null ? r.getConsultedAt() : r.getCreatedAt();
        var kstDateTime = targetDate.atZoneSameInstant(ZoneId.of("Asia/Seoul"));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.E", Locale.KOREAN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

        return RecordingDetailResponse.builder()
                .recordingId(r.getRecordingId())
                .date(kstDateTime.format(dateFormatter))
                .hospitalName(r.getHospitalName())
                .visitTime(kstDateTime.format(timeFormatter))
                .department(r.getDepartmentName())
                .doctorName(r.getDoctorName())
                .diagnosisName(r.getDiseaseName())
                .summary(r.getSummary() != null ? r.getSummary().getContent() : null)
                .fullTranscription(r.getTranscription() != null ? r.getTranscription().getRefinedText() : null)
                .audioUrl(r.getAudioUrl())
                .memo(r.getMemo())
                .build();
    }
}