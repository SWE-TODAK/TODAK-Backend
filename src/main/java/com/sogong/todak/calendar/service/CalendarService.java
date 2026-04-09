package com.sogong.todak.calendar.service;

import com.sogong.todak.calendar.dto.response.CalendarDetailResponse;
import com.sogong.todak.calendar.dto.response.CalendarMarkResponse;
import com.sogong.todak.recording.entity.Recording;
import com.sogong.todak.recording.repository.RecordingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final RecordingRepository recordingRepository;

    public CalendarMarkResponse getCalendarMarks(UUID userId, String month) {

        YearMonth yearMonth = YearMonth.parse(month);
        OffsetDateTime start = yearMonth.atDay(1).atStartOfDay(ZoneId.of("Asia/Seoul")).toOffsetDateTime();
        OffsetDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.of("Asia/Seoul")).toOffsetDateTime();

        List<Recording> recordings = recordingRepository.findByUser_UserIdAndCreatedAtBetween(userId, start, end);


         List<String> dates = recordings.stream()
                 .map(r -> r.getConsultedAt() != null ? r.getConsultedAt() : r.getCreatedAt())
                 .map(time -> time.atZoneSameInstant(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                 .distinct()
                 .collect(Collectors.toList());
        return new CalendarMarkResponse(dates);
    }

    public List<CalendarDetailResponse> getCalendarDetails(UUID userId, String date) {

        LocalDate localDate = LocalDate.parse(date);
        OffsetDateTime start = localDate.atStartOfDay(ZoneId.of("Asia/Seoul")).toOffsetDateTime();
        OffsetDateTime end = localDate.atTime(23, 59, 59).atZone(ZoneId.of("Asia/Seoul")).toOffsetDateTime();

        List<Recording> recordings = recordingRepository.findByUser_UserIdAndCreatedAtBetween(userId, start, end);

        return recordings.stream().map(CalendarDetailResponse::from).collect(Collectors.toList());
    }
}