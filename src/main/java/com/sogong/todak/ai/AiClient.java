package com.sogong.todak.ai;

import com.sogong.todak.ai.dto.AiSttResponse;
import com.sogong.todak.ai.dto.AiSummaryResponse;
import com.sogong.todak.ai.dto.SttByUrlRequest;
import com.sogong.todak.ai.dto.SummaryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AiClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${ai.base-url}")
    private String aiBaseUrl;

    @Value("${ai.internal-key}")
    private String internalKey;

    public AiSttResponse requestTranscriptionByUrl(SttByUrlRequest request) {
        RestClient restClient = restClientBuilder
                .baseUrl(aiBaseUrl)
                .build();

        return restClient.post()
                .uri("/internal/transcriptions/by-url")
                .header("X-Internal-Key", internalKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AiSttResponse.class);
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