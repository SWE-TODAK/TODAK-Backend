package com.sogong.todak.auth.oauth2.handler;

import com.sogong.todak.auth.oauth2.cookie.HttpCookieOAuth2AuthorizationRequestRepository;
import com.sogong.todak.auth.oauth2.exchange.ExchangeCodeStore;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OAuth2SuccessHandlerTest {

    @Test
    @DisplayName("platform 쿠키가 mobile이면 모바일 콜백으로 리다이렉트한다")
    void redirectsToMobileCallbackWhenPlatformCookieIsMobile() throws Exception {
        ExchangeCodeStore exchangeCodeStore = mock(ExchangeCodeStore.class);
        when(exchangeCodeStore.issue(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyBoolean()))
                .thenReturn("exchange-code");

        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(
                exchangeCodeStore,
                new HttpCookieOAuth2AuthorizationRequestRepository(false)
        );
        ReflectionTestUtils.setField(handler, "mobileCallbackUri", "todak://auth/callback");
        ReflectionTestUtils.setField(handler, "webCallbackUri", "http://3.34.99.179:3000/auth/callback");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(HttpCookieOAuth2AuthorizationRequestRepository.PLATFORM_PARAM_COOKIE_NAME, "mobile"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        OAuth2User principal = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "app_user_id", UUID.randomUUID().toString(),
                        "is_new_user", true
                ),
                "app_user_id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        handler.onAuthenticationSuccess(request, response, authentication);

        assertTrue(response.getRedirectedUrl().startsWith("todak://auth/callback?code=exchange-code"));
    }

    @Test
    @DisplayName("redirect_uri 쿠키가 있으면 platform보다 우선 사용한다")
    void redirectsToRedirectUriCookieFirst() throws Exception {
        ExchangeCodeStore exchangeCodeStore = mock(ExchangeCodeStore.class);
        when(exchangeCodeStore.issue(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyBoolean()))
                .thenReturn("exchange-code");

        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(
                exchangeCodeStore,
                new HttpCookieOAuth2AuthorizationRequestRepository(false)
        );
        ReflectionTestUtils.setField(handler, "mobileCallbackUri", "todak://auth/callback");
        ReflectionTestUtils.setField(handler, "webCallbackUri", "http://3.34.99.179:3000/auth/callback");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME,
                        "todak://custom/callback"),
                new Cookie(HttpCookieOAuth2AuthorizationRequestRepository.PLATFORM_PARAM_COOKIE_NAME, "web")
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        OAuth2User principal = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "app_user_id", UUID.randomUUID().toString(),
                        "is_new_user", false
                ),
                "app_user_id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        handler.onAuthenticationSuccess(request, response, authentication);

        assertTrue(response.getRedirectedUrl().startsWith("todak://custom/callback?code=exchange-code"));
    }
}
