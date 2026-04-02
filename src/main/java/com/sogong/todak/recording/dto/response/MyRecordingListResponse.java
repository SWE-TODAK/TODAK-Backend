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
public class MyRecordingListResponse {
    private UUID recordingId;
    private String date;
    private String hospitalName;
    private String visitTime;
    private String department;
    private String doctorName;
    private String summary;

    public static MyRecordingListResponse from(Recording recording) {
        OffsetDateTime targetDate = recording.getConsultedAt() != null ?
                recording.getConsultedAt() : recording.getCreatedAt();

        var kstDateTime = targetDate.atZoneSameInstant(ZoneId.of("Asia/Seoul"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.E", Locale.KOREAN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

        return MyRecordingListResponse.builder()
                .recordingId(recording.getRecordingId())
                .date(kstDateTime.format(dateFormatter))
                .hospitalName(recording.getHospitalName())
                .visitTime(kstDateTime.format(timeFormatter))
                .department(recording.getDepartmentName())
                .doctorName(recording.getDoctorName())
                .summary(recording.getSummary() != null ? recording.getSummary().getContent() : null)
                .build();
    }
}