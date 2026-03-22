package com.sogong.todak.auth.oauth2.cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.StringUtils;

@Slf4j
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180; // 3분

    private final boolean cookieSecure;
    private final boolean cookieHttpOnly;

    public HttpCookieOAuth2AuthorizationRequestRepository(boolean cookieSecure) {
        this.cookieSecure = cookieSecure;
        this.cookieHttpOnly = true;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        var opt = CookieUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        if (opt.isEmpty()) {
            log.warn("OAuth2 auth request cookie not found. uri={}, qs={}", request.getRequestURI(), request.getQueryString());
            return null;
        }

        var cookie = opt.get();
        OAuth2AuthorizationRequest authReq = CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class);
        if (authReq == null) {
            log.warn("OAuth2 auth request cookie present but deserialization returned null. cookieSize={}", cookie.getValue() == null ? -1 : cookie.getValue().length());
            return null;
        }

        // (선택) state mismatch 디버깅용
        String stateParam = request.getParameter("state");
        if (StringUtils.hasText(stateParam) && authReq.getState() != null && !stateParam.equals(authReq.getState())) {
            log.warn("OAuth2 state mismatch. paramState={}, cookieState={}", stateParam, authReq.getState());
            // mismatch면 null로 처리하는 게 안전 (다른 흐름/탭 오염 방지)
            return null;
        }

        return authReq;
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(response);
            return;
        }

        // 1) 인증 요청 정보 쿠키 저장
        String serialized = CookieUtils.serialize(authorizationRequest);
        CookieUtils.addCookie(
                response,
                OAUTH2_AUTH_REQUEST_COOKIE_NAME,
                serialized,
                COOKIE_EXPIRE_SECONDS,
                cookieHttpOnly,
                cookieSecure
        );

        // 2) 앱에서 보낸 redirect_uri 파라미터가 있다면 쿠키에 백업 (추후 활용)
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.hasText(redirectUriAfterLogin)) {
            CookieUtils.addCookie(
                    response,
                    REDIRECT_URI_PARAM_COOKIE_NAME,
                    redirectUriAfterLogin,
                    COOKIE_EXPIRE_SECONDS,
                    cookieHttpOnly,
                    cookieSecure
            );
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(response);
        return authorizationRequest;
    }

    public void removeAuthorizationRequestCookies(HttpServletResponse response) {
        CookieUtils.deleteCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        CookieUtils.deleteCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }
}