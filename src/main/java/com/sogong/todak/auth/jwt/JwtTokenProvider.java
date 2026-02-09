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
    private final SecretKey key;
    private final long accessExpMin;
    private final long refreshExpDays;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-exp-min:30}") long accessExpMin,
            @Value("${jwt.refresh-exp-days:14}") long refreshExpDays
    ) {
        // 보안 권장사항: 서명 키는 최소 256비트(32바이트) 이상이어야 합니다.
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpMin = accessExpMin;
        this.refreshExpDays = refreshExpDays;
    }

    /** Access Token 생성: 유저의 UUID와 권한을 포함 */
    public String createAccessToken(UUID userId, String role) {
        return createToken(userId, role, Duration.ofMinutes(accessExpMin));
    }

    /** Refresh Token 생성: DB의 UserAuth 등과 연계하여 갱신 용도로 사용 */
    public String createRefreshToken(UUID userId) {
        return createToken(userId, null, Duration.ofDays(refreshExpDays));
    }

    private String createToken(UUID userId, String role, Duration duration) {
        Instant now = Instant.now();
        JwtBuilder builder = Jwts.builder()
                .subject(userId.toString()) // 모든 테이블의 공통 PK인 UUID 사용
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(duration)))
                .signWith(key);

        if (role != null) {
            builder.claim(ROLE_KEY, role);
        }

        return builder.compact();
    }

    /** * 토큰 유효성 검증 */
    public boolean validate(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    public UUID getUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public String getRole(String token) {
        Object role = parseClaims(token).get(ROLE_KEY);
        // 자체 로그인 사용자와 소셜 사용자 모두 최소 ROLE_USER 권한을 가짐을 전제로 합니다.
        return role == null ? "ROLE_USER" : role.toString();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}