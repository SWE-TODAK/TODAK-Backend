package com.sogong.todak.auth.oauth2.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.util.SerializationUtils;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public final class CookieUtils {

    private CookieUtils() {}

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findFirst();
    }

    public static void deleteCookie(HttpServletResponse response, String name) {
        // 기본값: httpOnly=true, secure=false (로컬 기준)
        // secure는 운영에서 cookieSecure=true로 세팅된 repo/handler에서 오버로드 사용 추천
        deleteCookie(response, name, true, false);
    }

    /**
     * ResponseCookie로 SameSite=Lax 강제 (OAuth2 리다이렉트 시 쿠키 유실 방지)
     */
    public static void addCookie(
            HttpServletResponse response,
            String name,
            String value,
            int maxAgeSeconds,
            boolean httpOnly,
            boolean secure
    ) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(httpOnly)
                .secure(secure)
                .sameSite("Lax")
                .maxAge(maxAgeSeconds)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void deleteCookie(HttpServletResponse response, String name, boolean httpOnly, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .path("/")
                .httpOnly(httpOnly)
                .secure(secure)
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * ✅ 객체 -> (Java Serialization) -> Base64URL
     * OAuth2AuthorizationRequest는 Serializable이라 이 방식이 가장 호환성이 좋음.
     */
    public static String serialize(Object object) {
        byte[] bytes = SerializationUtils.serialize(object);
        if (bytes == null) {
            throw new IllegalStateException("Failed to serialize object (result is null)");
        }
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    /**
     * ✅ Base64URL -> (Java Deserialization) -> 객체
     * 실패 시 null 반환(인증 재시도 유도)
     */
    public static <T> T deserialize(Cookie cookie, Class<T> clazz) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cookie.getValue());
            Object obj = SerializationUtils.deserialize(decoded);
            if (obj == null) return null;
            return clazz.cast(obj);
        } catch (Exception e) {
            return null;
        }
    }
}