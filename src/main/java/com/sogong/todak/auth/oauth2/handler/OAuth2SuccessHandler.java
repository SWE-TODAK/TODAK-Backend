package com.sogong.todak.auth.oauth2.handler;

import com.sogong.todak.auth.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth2.redirect-success:http://localhost:3000/auth/callback}")
    private String successRedirectUrl;

    @Value("${app.jwt.cookie.access-name:ACCESS_TOKEN}")
    private String accessCookieName;

    @Value("${app.jwt.cookie.refresh-name:REFRESH_TOKEN}")
    private String refreshCookieName;

    @Value("${app.jwt.access-ttl-seconds:1800}")
    private long accessTtlSeconds;

    @Value("${app.jwt.refresh-ttl-seconds:1209600}")
    private long refreshTtlSeconds;

    @Value("${app.jwt.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.jwt.cookie.same-site:Lax}")
    private String sameSite;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        try {
            // 1. Principal로부터 최적화된 방식으로 UUID 추출
            UUID userId = extractUserId(authentication);

            // 2. JWT 토큰 발급 (userId는 모든 테이블의 공통 식별자)
            String accessToken = jwtTokenProvider.createAccessToken(userId, "ROLE_USER");
            String refreshToken = jwtTokenProvider.createRefreshToken(userId);

            // 3. HTTP Only 쿠키 설정
            setTokenCookies(response, accessToken, refreshToken);

            log.info("OAuth2 Login Success: userId={}, redirecting to callback", userId);
            response.sendRedirect(successRedirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 Success Handler Error: ", e);

            // 실패 시 에러 코드를 포함하여 안전하게 리다이렉트
            String errorUrl = UriComponentsBuilder.fromUriString(successRedirectUrl)
                    .queryParam("success", false)
                    .queryParam("error", "authentication_failed")
                    .build().toUriString();
            response.sendRedirect(errorUrl);
        }
    }

    /**
     * CustomOAuth2UserService에서 enriched attributes에 담은 UUID를 추출합니다.
     */
    private UUID extractUserId(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof OAuth2User principal)) {
            throw new IllegalArgumentException("Invalid principal type: Expected OAuth2User");
        }

        Object attr = principal.getAttribute("app_user_id");
        if (attr == null) {
            throw new IllegalStateException("Required attribute 'app_user_id' is missing");
        }

        // 이미 UUID 객체라면 바로 반환, 문자열이라면 파싱 (유연한 처리)
        if (attr instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(attr.toString());
    }

    private void setTokenCookies(HttpServletResponse response, String access, String refresh) {
        addCookie(response, buildCookie(accessCookieName, access, accessTtlSeconds));
        addCookie(response, buildCookie(refreshCookieName, refresh, refreshTtlSeconds));
    }

    private ResponseCookie buildCookie(String name, String value, long maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(sameSite)
                .build();
    }

    private void addCookie(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}