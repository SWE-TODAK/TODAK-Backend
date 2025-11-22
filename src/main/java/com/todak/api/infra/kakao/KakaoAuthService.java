package com.todak.api.infra.kakao;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.user-info-uri}")
    private String userInfoUri;

    @Value("${kakao.client-id}")
    private String clientId;

    // application.yml에 redirect-uri 추가 필요 (프론트엔드 주소와 일치해야 함)
    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    /**
     * 1. 인가 코드로 카카오 Access Token 받아오기 (새로 추가된 메서드!)
     */
    public String getAccessToken(String authCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri); // 카카오에 등록한 주소와 100% 일치해야 함
        params.add("code", authCode);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                request,
                KakaoTokenResponse.class
        );

        return response.getBody().getAccessToken();
    }

    /**
     * 2. Access Token으로 유저 정보 가져오기 (기존 메서드)
     */
    public Long getKakaoUserId(String accessToken) {
        // ... (기존 코드 유지) ...
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<KakaoUserInfoResponse> response = restTemplate.exchange(
                    userInfoUri,
                    HttpMethod.GET,
                    entity,
                    KakaoUserInfoResponse.class
            );

            return response.getBody().getId();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("카카오 토큰이 유효하지 않습니다.");
        }
    }

    @Getter
    static class KakaoUserInfoResponse {
        private Long id;
    }

    @Getter
    static class KakaoTokenResponse {
        private String access_token;
        private String refresh_token;
        // 필요한 필드 더 추가 가능
        public String getAccessToken() { return access_token; }
    }
}