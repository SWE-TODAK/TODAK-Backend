package com.sogong.todak.ai;

import com.sogong.todak.ai.dto.AiSttResponse;
import com.sogong.todak.ai.dto.AiSummaryResponse;
import com.sogong.todak.ai.dto.SttByUrlRequest;
import com.sogong.todak.ai.dto.SummaryRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class AiClient {

    private final RestClient restClient;
    private final String internalKey;

    // 생성자 주입을 통해 RestClient를 한 번만 빌드합니다.
    public AiClient(
            RestClient.Builder restClientBuilder,
            @Value("${ai.base-url}") String aiBaseUrl,
            @Value("${ai.internal-key}") String internalKey
    ) {
        // 1. Simple 팩토리 생성
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 2. 타임아웃 설정 (단위: 밀리초 ms)
        factory.setConnectTimeout(5000);   // 5초
        factory.setReadTimeout(300000);    // 300초 (5분) - AI 응답 대기용

        // 3. 빌더에 주입
        this.restClient = restClientBuilder
                .baseUrl(aiBaseUrl)
                .requestFactory(factory)
                .build();

        this.internalKey = internalKey;
    }

    /**
     * STT 변환 요청
     */
    public AiSttResponse requestTranscriptionByUrl(SttByUrlRequest request) {
        log.info(">>>> Sending STT request to AI server. recordingId={}", request.recordingId());

        return restClient.post()
                .uri("/internal/transcriptions/by-url")
                .header("X-Internal-Key", internalKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                // 4xx, 5xx 에러 발생 시 로그를 남기고 예외를 던짐
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error(">>>> AI STT API Error: Status Code {}", res.getStatusCode());
                    throw new RuntimeException("AI STT API 호출 실패: " + res.getStatusCode());
                })
                .body(AiSttResponse.class);
    }

    /**
     * 요약 요청
     */
    public AiSummaryResponse requestSummary(SummaryRequest request) {
        log.info(">>>> Sending Summary request to AI server. recordingId={}", request.recordingId());

        AiSummaryResponse response = restClient.post()
                .uri("/internal/summarizes")
                .header("X-Internal-Key", internalKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error(">>>> AI Summary API Error: Status Code {}", res.getStatusCode());
                    throw new RuntimeException("AI 요약 API 호출 실패: " + res.getStatusCode());
                })
                .body(AiSummaryResponse.class);

        // 응답 널 체크 (정상 응답이지만 바디가 비어있는 경우 대비)
        if (response == null || response.data() == null) {
            log.error(">>>> AI Summary response or data is null for recordingId={}", request.recordingId());
            throw new IllegalStateException("AI summary response data is null");
        }

        return response;
    }
}