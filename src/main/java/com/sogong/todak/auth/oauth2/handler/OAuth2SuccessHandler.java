package com.sogong.todak.auth.oauth2.handler;

import com.sogong.todak.auth.oauth2.exchange.ExchangeCodeStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.oauth2.mobile-callback-uri:todak://auth/callback}")
    private String mobileCallbackUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        String targetUrl;

        try {
            OAuth2User principal = requireOAuth2User(authentication);

            UUID userId = extractUserId(principal);
            boolean isNewUser = extractIsNewUser(principal);

            String code = exchangeCodeStore.issue(userId, isNewUser);

            // ✅ success 파라미터는 선택. 코드만으로 충분.
            targetUrl = UriComponentsBuilder.fromUriString(mobileCallbackUri)
                    .queryParam("code", code)
                    .build()
                    .toUriString();

            log.info("OAuth2 Success: userId={}, issued exchangeCode", userId);

        } catch (Exception e) {
            log.error("OAuth2 SuccessHandler failed", e);

            targetUrl = UriComponentsBuilder.fromUriString(mobileCallbackUri)
                    .queryParam("error", "auth_failed")
                    .build()
                    .toUriString();
        }

        response.sendRedirect(targetUrl);
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
        if (attr instanceof Boolean bool) return bool;
        if (attr instanceof String str) return Boolean.parseBoolean(str);
        return false;
    }
}