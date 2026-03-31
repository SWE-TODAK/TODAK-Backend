package com.sogong.todak.ai;

import com.sogong.todak.ai.dto.AiSttResponse;
import com.sogong.todak.ai.dto.AiSummaryResponse;
import com.sogong.todak.ai.dto.SttByUrlRequest;
import com.sogong.todak.ai.dto.SummaryRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${ai.base-url}")
    private String aiBaseUrl;

    @Value("${ai.internal-key}")
    private String internalKey;

    public AiSttResponse requestTranscriptionByUrl(SttByUrlRequest request) {
//        RestClient restClient = restClientBuilder
//                .baseUrl(aiBaseUrl)
//                .build();
//
//        return restClient.post()
//                .uri("/internal/transcriptions/by-url")
//                .header("X-Internal-Key", internalKey)
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(request)
//                .retrieve()
//                .body(AiSttResponse.class);
        // [확인 1] 실제 주소와 키가 주입되었는지 로그로 확인
        //System.out.println(">>>> [AiClient DEBUG] Target URL: " + aiBaseUrl);
        //System.out.println(">>>> [AiClient DEBUG] Internal Key Length: " + (internalKey != null ? internalKey.length() : "NULL"));

        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(aiBaseUrl)
                    .build();

            //log.info(">>>> FastAPI로 요청을 보냅니다... (ID: {})", request.recordingId());

            AiSttResponse response = restClient.post()
                    .uri("/internal/transcriptions/by-url")
                    .header("X-Internal-Key", internalKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AiSttResponse.class);

            //log.info(">>>> FastAPI 응답 수신 성공!");
            return response;

        } catch (Exception e) {
            // [확인 2] 에러가 났다면 절대 놓치지 않고 출력
            //System.err.println(">>>> [AiClient FATAL ERROR] 호출 실패 원인: " + e.getMessage());
            //e.printStackTrace();
            throw e;
        }
    }

    public AiSummaryResponse requestSummary(SummaryRequest request) {
        RestClient restClient = restClientBuilder
                .baseUrl(aiBaseUrl)
                .build();

        AiSummaryResponse response = restClient.post()
                .uri("/internal/summarizes")
                .header("X-Internal-Key", internalKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AiSummaryResponse.class);

        if (response == null) {
            throw new IllegalStateException("AI summary response is null");
        }

        if (response.data() == null) {
            throw new IllegalStateException("AI summary response data is null");
        }

        return response;
    }
}