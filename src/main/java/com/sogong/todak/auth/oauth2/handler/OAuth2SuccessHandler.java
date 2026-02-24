package com.sogong.todak.auth.oauth2.handler;

import com.sogong.todak.auth.oauth2.cookie.HttpCookieOAuth2AuthorizationRequestRepository;
import com.sogong.todak.auth.oauth2.exchange.ExchangeCodeStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final String ATTR_USER_ID = "app_user_id";
    private static final String ATTR_IS_NEW_USER = "is_new_user";

    private final ExchangeCodeStore exchangeCodeStore;

    // ✅ 추가: 성공 시 쿠키 정리
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthRepo;

    @Value("${app.oauth2.mobile-callback-uri:todak://auth/callback}")
    private String mobileCallbackUri;

    // (선택) 웹 테스트용 fallback
    @Value("${app.oauth2.web-callback-uri:http://localhost:3000/auth/callback}")
    private String webCallbackUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        // ✅ 캐시/재요청 억제 (브라우저가 redirect 응답을 이상하게 재사용하는 케이스 방어)
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");

        String targetUrl;

        try {
            OAuth2User principal = requireOAuth2User(authentication);

            UUID userId = extractUserId(principal);
            boolean isNewUser = extractIsNewUser(principal);

            String code = exchangeCodeStore.issue(userId, isNewUser);

            // ✅ 성공 시점에 auth request 관련 쿠키 제거
            cookieAuthRepo.removeAuthorizationRequestCookies(response);

            // ✅ 브라우저에서 todak:// 스킴을 못 열면 UX가 꼬일 수 있으니,
            // 테스트 환경에서는 webCallbackUri로 보내도 됨 (원하면 조건 분기)
            boolean isBrowser = isLikelyBrowser(request);
            String callback = isBrowser ? webCallbackUri : mobileCallbackUri;

            targetUrl = UriComponentsBuilder.fromUriString(callback)
                    .queryParam("code", code)
                    .build()
                    .toUriString();

            log.info("OAuth2 Success: userId={}, issued exchangeCode, redirect={}", userId, callback);

        } catch (Exception e) {
            log.error("OAuth2 SuccessHandler failed", e);

            // 실패도 쿠키 정리 (재시도 UX 위해)
            cookieAuthRepo.removeAuthorizationRequestCookies(response);

            targetUrl = UriComponentsBuilder.fromUriString(webCallbackUri)
                    .queryParam("success", false)
                    .queryParam("error", "auth_failed")
                    .build()
                    .toUriString();
        }

        response.sendRedirect(targetUrl);
    }

    private boolean isLikelyBrowser(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        // 매우 러프하지만 로컬에서 “브라우저 테스트”일 때 webCallback로 보내기 좋음
        return ua != null && (ua.contains("Chrome") || ua.contains("Edg") || ua.contains("Safari") || ua.contains("Firefox"));
    }

    private OAuth2User requireOAuth2User(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof OAuth2User oAuth2User)) {
            throw new IllegalArgumentException("Invalid principal type: " + principal.getClass());
        }
        return oAuth2User;
    }

    private UUID extractUserId(OAuth2User principal) {
        Object attr = principal.getAttribute(ATTR_USER_ID);
        if (attr == null) throw new IllegalStateException(ATTR_USER_ID + " is missing");
        return (attr instanceof UUID uuid) ? uuid : UUID.fromString(attr.toString());
    }

    private boolean extractIsNewUser(OAuth2User principal) {
        Object attr = principal.getAttribute(ATTR_IS_NEW_USER);
        if (attr instanceof Boolean bool) return bool;
        if (attr instanceof String str) return Boolean.parseBoolean(str);
        return false;
    }
}