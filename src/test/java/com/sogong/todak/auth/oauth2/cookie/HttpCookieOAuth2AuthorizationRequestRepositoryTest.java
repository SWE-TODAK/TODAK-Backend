package com.sogong.todak.auth.oauth2.cookie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

    @Test
    @DisplayName("OAuth 시작 요청의 redirect_uri와 platform을 쿠키에 저장한다")
    void saveAuthorizationRequestStoresRedirectUriAndPlatformCookies() {
        HttpCookieOAuth2AuthorizationRequestRepository repository =
                new HttpCookieOAuth2AuthorizationRequestRepository(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME,
                "todak://auth/callback");
        request.setParameter(HttpCookieOAuth2AuthorizationRequestRepository.PLATFORM_PARAM_COOKIE_NAME,
                "mobile");

        MockHttpServletResponse response = new MockHttpServletResponse();

        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .clientId("client-id")
                .redirectUri("http://54.116.64.125:8080/login/oauth2/code/kakao")
                .state("state")
                .authorizationRequestUri("https://kauth.kakao.com/oauth/authorize?client_id=client-id")
                .build();

        repository.saveAuthorizationRequest(authorizationRequest, request, response);

        assertTrue(response.getHeaders("Set-Cookie").stream()
                .anyMatch(value -> value.contains("redirect_uri=todak://auth/callback")));
        assertTrue(response.getHeaders("Set-Cookie").stream()
                .anyMatch(value -> value.contains("platform=mobile")));
    }
}
