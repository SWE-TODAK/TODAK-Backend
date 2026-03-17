package com.sogong.todak.auth.oauth2.handler;

import com.sogong.todak.auth.oauth2.cookie.CookieUtils;
import com.sogong.todak.auth.oauth2.cookie.HttpCookieOAuth2AuthorizationRequestRepository;
import com.sogong.todak.auth.oauth2.exchange.ExchangeCodeStore;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final String ATTR_USER_ID = "app_user_id";
    private static final String ATTR_IS_NEW_USER = "is_new_user";
    private static final String PLATFORM_WEB = "web";
    private static final String PLATFORM_MOBILE = "mobile";

    private final ExchangeCodeStore exchangeCodeStore;
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> cookieAuthRepo;

    @Value("${app.oauth2.mobile-callback-uri:todak://auth/callback}")
    private String mobileCallbackUri;

    @Value("${app.oauth2.web-callback-uri:http://localhost:3000/auth/callback}")
    private String webCallbackUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");

        String targetUrl;

        try {
            OAuth2User principal = requireOAuth2User(authentication);

            UUID userId = extractUserId(principal);
            boolean isNewUser = extractIsNewUser(principal);

            String code = exchangeCodeStore.issue(userId, isNewUser);

            String callback = resolveCallback(request);
            clearAuthenticationAttributes(request, response);

            targetUrl = UriComponentsBuilder.fromUriString(callback)
                    .queryParam("code", code)
                    .build()
                    .toUriString();

            log.info("OAuth2 Success: userId={}, issued exchangeCode, platform={}, redirect={}",
                    userId, resolvePlatform(request), callback);

        } catch (Exception e) {
            log.error("OAuth2 SuccessHandler failed", e);

            clearAuthenticationAttributes(request, response);

            targetUrl = UriComponentsBuilder.fromUriString(webCallbackUri)
                    .queryParam("success", false)
                    .queryParam("error", "auth_failed")
                    .build()
                    .toUriString();
        }

        response.sendRedirect(targetUrl);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        if (cookieAuthRepo instanceof HttpCookieOAuth2AuthorizationRequestRepository repository) {
            repository.removeAuthorizationRequestCookies(response);
        }
    }

    private String resolveCallback(HttpServletRequest request) {
        String redirectUri = readCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.hasText(redirectUri)) {
            return redirectUri;
        }

        String platform = resolvePlatform(request);

        if (PLATFORM_MOBILE.equalsIgnoreCase(platform)) {
            return mobileCallbackUri;
        }

        if (PLATFORM_WEB.equalsIgnoreCase(platform)) {
            return webCallbackUri;
        }

        return isLikelyBrowser(request) ? webCallbackUri : mobileCallbackUri;
    }

    private String resolvePlatform(HttpServletRequest request) {
        String platform = request.getParameter(HttpCookieOAuth2AuthorizationRequestRepository.PLATFORM_PARAM_COOKIE_NAME);
        if (StringUtils.hasText(platform)) {
            return platform;
        }

        return readCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.PLATFORM_PARAM_COOKIE_NAME);
    }

    private String readCookie(HttpServletRequest request, String cookieName) {
        return CookieUtils.getCookie(request, cookieName)
                .map(Cookie::getValue)
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    private boolean isLikelyBrowser(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return ua != null && (
                ua.contains("Chrome")
                        || ua.contains("Edg")
                        || ua.contains("Safari")
                        || ua.contains("Firefox")
        );
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
        if (attr == null) {
            throw new IllegalStateException(ATTR_USER_ID + " is missing");
        }
        return (attr instanceof UUID uuid) ? uuid : UUID.fromString(attr.toString());
    }

    private boolean extractIsNewUser(OAuth2User principal) {
        Object attr = principal.getAttribute(ATTR_IS_NEW_USER);
        if (attr instanceof Boolean bool) {
            return bool;
        }
        if (attr instanceof String str) {
            return Boolean.parseBoolean(str);
        }
        return false;
    }
}
