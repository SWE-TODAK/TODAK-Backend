package com.sogong.todak.calendar.dto.response;

import com.sogong.todak.recording.entity.Recording;
import lombok.Getter;
import lombok.Builder;

import java.util.UUID;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Getter
@Builder
public class CalendarDetailResponse {
    private UUID recordingId;
    private String dateDisplay;
    private String hospitalName;
    private String visitTime;
    private String summary;

    public static CalendarDetailResponse from(Recording r){
        var targetDate = r.getConsultedAt() != null ? r.getConsultedAt() : r.getCreatedAt();
        var kstDateTime = targetDate.atZoneSameInstant(ZoneId.of("Asia/Seoul"));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 E요일", Locale.KOREAN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

        return CalendarDetailResponse.builder()
                .recordingId(r.getRecordingId())
                .dateDisplay(kstDateTime.format(dateFormatter))
                .visitTime(kstDateTime.format(timeFormatter))
                .hospitalName(r.getHospitalName())
                .summary(r.getSummary() != null ? r.getSummary().getContent() : null)
                .build();
    }
}