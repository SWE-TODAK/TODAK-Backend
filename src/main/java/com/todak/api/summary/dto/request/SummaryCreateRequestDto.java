package com.todak.api.summary.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SummaryCreateRequestDto {
    private Long consultationId;
    private Long recordingId;
    //STT는 이미 DB에 있으며, 요약은 Spring이 AI 서버에 요청할 때 transcript를 넣는다.
}
