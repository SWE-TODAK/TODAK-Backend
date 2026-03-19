package com.sogong.todak.ai;

import com.sogong.todak.ai.dto.AiSttResponse;
import com.sogong.todak.ai.dto.SttByUrlRequest;
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
}