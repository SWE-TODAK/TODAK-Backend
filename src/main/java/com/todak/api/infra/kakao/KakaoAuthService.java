package com.todak.api.infra.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final RestTemplate restTemplate;

    @Value("${kakao.user-info-uri}")
    private String userInfoUri;

    // 앱 방식 로그인이므로 redirectUri는 사실상 사용되지 않지만 에러 방지를 위해 둠
    @Value("${kakao.redirect-uri:}")
    private String redirectUri;

    @Value("${kakao.client-id}")
    private String clientId;

    // (이 메서드는 앱 로그인 방식에서 사용되지 않으므로 무시하셔도 됩니다)
    public String getAccessToken(String authCode) {
        return null;
    }

    /**
     * 2. Access Token으로 유저 정보 가져오기
     */
    public Long getKakaoUserId(String accessToken) {
        log.info("[KakaoAuthService] 사용자 정보 요청. Token 존재 여부: {}", (accessToken != null && !accessToken.isEmpty()));
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<KakaoUserInfoResponse> response = restTemplate.exchange(
                    userInfoUri,
                    HttpMethod.GET,
                    entity,
                    KakaoUserInfoResponse.class
            );

            log.info("✅ [KakaoAuthService] 사용자 ID 획득: {}", response.getBody().getId());
            return response.getBody().getId();

        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("🚨 카카오 API 호출 실패 (상태: {}): {}", e.getStatusCode(), errorBody);
            // 여기서 401이 뜨면 토큰 자체가 만료되었거나 잘못된 문자열인 경우입니다.
            throw new RuntimeException("카카오 유저 정보 조회 실패: " + errorBody);
        }
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class KakaoUserInfoResponse {
        @JsonProperty("id")
        private Long id;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class KakaoTokenResponse {
        @JsonProperty("access_token")
        private String access_token;
        public String getAccessToken() { return access_token; }
    }
}