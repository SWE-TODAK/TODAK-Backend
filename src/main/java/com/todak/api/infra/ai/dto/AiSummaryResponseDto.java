package com.todak.api.infra.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AI Summary API 응답 DTO
 * {
 *   "status": 200,
 *   "message": "요약 생성이 완료되었습니다.",
 *   "data": {
 *       "consultationId": "...",
 *       "recordingId": "...",
 *       "content": { ... },
 *       "tags": { ... },
 *       "meta": { ... }
 *   }
 * }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSummaryResponseDto {

    private Integer status;          // 200
    private String message;          // "요약 생성이 완료되었습니다."
    private SummaryData data;        // 실제 요약 정보

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SummaryData {
        private String consultationId;
        private String recordingId;

        // 요약 내용 객체(JSON 전체)
        private Object content;

        // 키워드, 위험도 등 태그 정보
        private Object tags;

        // 메타 데이터 (AI 모델 정보 등)
        private Object meta;
    }
}
