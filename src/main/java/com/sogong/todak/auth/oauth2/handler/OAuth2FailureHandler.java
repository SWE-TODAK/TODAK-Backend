package com.sogong.todak.auth.oauth2.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
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

        // 1. 서버 로그에는 상세 원인 기록
        log.error("OAuth2 Authentication Failed: ", exception);

        // 2. 프론트엔드에 전달할 에러 코드 분류
        String errorCode = classify(exception);

        // 3. 안전한 URL 생성 (상세 메시지는 보안을 위해 제외하거나 마스킹 처리)
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

        // 사용자가 권한 승인을 거부한 경우
        if (message.contains("access_denied")) {
            return "user_cancelled";
        }

        // 제공자(카카오 등) 관련 설정 오류
        if (message.contains("unsupported_provider") || message.contains("invalid_provider_id")) {
            return "provider_config_error";
        }

        // 그 외 인증 과정 중 발생한 에러
        return "authentication_error";
    }


}