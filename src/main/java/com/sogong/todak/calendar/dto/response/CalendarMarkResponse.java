package com.sogong.todak.calendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CalendarMarkResponse {
    private List<String> dates;
}
