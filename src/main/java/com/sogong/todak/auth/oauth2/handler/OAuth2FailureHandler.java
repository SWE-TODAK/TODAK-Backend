package com.sogong.todak.auth.oauth2.handler;

import com.sogong.todak.auth.oauth2.cookie.CookieUtils;
import com.sogong.todak.auth.oauth2.cookie.HttpCookieOAuth2AuthorizationRequestRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-failure:http://localhost:3000/auth/callback}")
    private String failureRedirectUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        // ---- 공통 로그 ----
        log.error("OAuth2 Failure Request URI      = {}", request.getRequestURI());
        log.error("OAuth2 Failure QueryString     = {}", request.getQueryString());
        log.error("OAuth2 Failure code param      = {}", request.getParameter("code"));
        log.error("OAuth2 Failure state param     = {}", request.getParameter("state"));
        log.error("OAuth2 Failure error param     = {}", request.getParameter("error"));
        log.error("OAuth2 Failure error_desc param= {}", request.getParameter("error_description"));
        log.error("OAuth2 Failure Exception Message = {}", exception.getMessage());
        log.error("OAuth2 Failure Exception Class   = {}", exception.getClass().getName());

        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                log.error("OAuth2 Failure Cookie [{}] = {}", cookie.getName(), cookie.getValue());
            }
        } else {
            log.error("OAuth2 Failure Cookies = NONE");
        }
        log.error("OAuth2 Authentication Failed: ", exception);

        // ---- 중복 콜백/새로고침/만료 케이스 처리 ----
        String providerError = request.getParameter("error");
        boolean hasProviderErrorParam = StringUtils.hasText(providerError);

        // 우리가 저장한 auth request 쿠키 존재 여부
        boolean hasAuthRequestCookie = CookieUtils.getCookie(
                request,
                HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTH_REQUEST_COOKIE_NAME
        ).isPresent();

        // error 파라미터는 없는데 쿠키도 없다?
        // => 정상 로그인 성공 후 콜백 URL 새로고침/중복 진입/만료 등
        if (!hasProviderErrorParam && !hasAuthRequestCookie) {
            String targetUrl = UriComponentsBuilder.fromUriString(failureRedirectUrl)
                    .queryParam("success", false)
                    .queryParam("error", "auth_request_missing") // 프론트에서 안내 문구 처리
                    .build()
                    .toUriString();

            log.warn("OAuth2 failure treated as duplicate/expired callback. Redirecting to={}", targetUrl);
            response.sendRedirect(targetUrl);
            return;
        }

        String errorCode = classify(exception);

        String targetUrl = UriComponentsBuilder.fromUriString(failureRedirectUrl)
                .queryParam("success", false)
                .queryParam("error", errorCode)
                .build()
                .toUriString();

        response.sendRedirect(targetUrl);
    }

    /**
     * 예외 타입에 따른 에러 코드 분류
     */
    private String classify(AuthenticationException e) {
        if (e == null) return "auth_failed";
        String message = e.getMessage() != null ? e.getMessage() : "";

        if (message.contains("access_denied")) {
            return "user_cancelled";
        }
        if (message.contains("unsupported_provider") || message.contains("invalid_provider_id")) {
            return "provider_config_error";
        }

        // ✅ 쿠키/세션/요청 저장소에서 못 찾는 케이스를 좀 더 명확히
        if (message.contains("authorization_request_not_found")) {
            return "auth_request_not_found";
        }

        return "authentication_error";
    }
}