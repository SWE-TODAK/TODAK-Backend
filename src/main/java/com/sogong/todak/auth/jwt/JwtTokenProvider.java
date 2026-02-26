package com.sogong.todak.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String ROLE_KEY = "role";
    private static final String TOKEN_TYPE_KEY = "token_type"; // ✅ access/refresh 구분용
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;
    private final String issuer; // 선택이지만 추천

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-exp-min:30}") long accessExpMin,
            @Value("${jwt.refresh-exp-days:14}") long refreshExpDays,
            @Value("${jwt.issuer:todak}") String issuer
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("jwt.secret must be at least 32 bytes (256 bits) for HS256");
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTtl = Duration.ofMinutes(accessExpMin);
        this.refreshTtl = Duration.ofDays(refreshExpDays);
        this.issuer = issuer;
    }

    /** Access Token 생성: userId + role + token_type=access */
    public String createAccessToken(UUID userId, String role) {
        return createToken(userId, role, ACCESS, accessTtl);
    }

    /** Refresh Token 생성: userId + token_type=refresh (role 불필요) */
    public String createRefreshToken(UUID userId) {
        return createToken(userId, null, REFRESH, refreshTtl);
    }

    /** accessToken 만료(초) - AuthResponse.expiresInSeconds에 그대로 넣기  */
    public long getAccessExpiresInSeconds() {
        return accessTtl.getSeconds();
    }

    /** refreshToken 만료(초) - 필요하면 사용 */
    public long getRefreshExpiresInSeconds() {
        return refreshTtl.getSeconds();
    }

    private String createToken(UUID userId, String role, String tokenType, Duration ttl) {
        Instant now = Instant.now();

        JwtBuilder builder = Jwts.builder()
                .issuer(issuer)
                .subject(userId.toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .claim(TOKEN_TYPE_KEY, tokenType)        // ✅ access/refresh 구분
                .signWith(key);

        if (role != null) {
            builder.claim(ROLE_KEY, role);
        }

        return builder.compact();
    }

    /**
     * 토큰 유효성 검증 (boolean 버전)
     * - access/refresh 구분 없이 서명/만료만 체크.
     */
    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 예외를 던지는 검증/파싱 버전
     * - 필터/서비스에서 만료/형식오류를 구분해서 처리하고 싶을 때 사용
     */
    public Claims parseClaimsOrThrow(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Claims parseClaims(String token) {
        return parseClaimsOrThrow(token);
    }

    public UUID getUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public String getRole(String token) {
        Object role = parseClaims(token).get(ROLE_KEY);
        return role == null ? "ROLE_USER" : role.toString();
    }

    /** 토큰 타입 확인: access인지 refresh인지 판별 가능 */
    public boolean isAccessToken(String token) {
        return ACCESS.equals(getTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return REFRESH.equals(getTokenType(token));
    }

    public String getTokenType(String token) {
        Object t = parseClaims(token).get(TOKEN_TYPE_KEY);
        return t == null ? null : t.toString();
    }
}