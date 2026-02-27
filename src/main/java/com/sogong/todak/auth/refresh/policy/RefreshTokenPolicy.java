package com.sogong.todak.auth.refresh.policy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class RefreshTokenPolicy {

    private final SecureRandom secureRandom = new SecureRandom();
    private final long refreshExpDays;
    private final int tokenBytes;

    public RefreshTokenPolicy(
            @Value("${jwt.refresh-exp-days:14}") long refreshExpDays,
            @Value("${app.oauth2.refresh-token-bytes:32}") int tokenBytes
    ) {
        this.refreshExpDays = refreshExpDays;
        this.tokenBytes = tokenBytes;
    }

    /**
     * 클라이언트에게 전달할 원문 토큰 생성
     */
    public String generateRawToken() {
        byte[] bytes = new byte[tokenBytes];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * DB 저장용 해시값 생성 (SHA-256)
     */
    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 만료 시각 계산 (UTC 기준 통일)
     */
    public OffsetDateTime expiresAtFromNow() {
        return OffsetDateTime.now(ZoneOffset.UTC).plusDays(refreshExpDays);
    }

    /**
     * Redis 등 TTL 설정 시 사용
     */
    public Duration getRefreshTtl() {
        return Duration.ofDays(refreshExpDays);
    }

    /**
     * [추가 권장] 토큰 검증 시 안전한 비교 로직
     * 두 해시값이 일치하는지 시간 차 공격(Timing Attack) 없이 비교
     */
    public boolean verify(String rawToken, String storedHash) {
        String hashedRaw = hash(rawToken);
        return MessageDigest.isEqual(
                hashedRaw.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }
}