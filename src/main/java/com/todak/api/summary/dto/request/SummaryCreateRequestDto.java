package com.todak.api.summary.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SummaryCreateRequestDto {

    private Long consultationId;  // optional
    private Long recordingId;     // optional

    private String content;       // 요약 본문
    private String tags;          // 주요 태그 (콤마 or JSON 문자열)
}
