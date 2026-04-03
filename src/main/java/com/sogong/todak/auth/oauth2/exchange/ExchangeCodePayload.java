package com.sogong.todak.auth.oauth2.exchange;

import com.sogong.todak.auth.dto.response.AuthResult;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * exchangeCode에 매핑되는 payload
 * - userId: 누구의 코드인지
 * - authResult: OAuth 인증 결과
 * - expiresAt: 만료 시간
 */
public record ExchangeCodePayload(
        UUID userId,
        AuthResult authResult,
        Instant expiresAt
) {
    public ExchangeCodePayload {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(authResult, "authResult must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    public boolean isExpired(Instant now) {
        // 만료시각 포함(<=) 정책
        return !expiresAt.isAfter(now);
    }
}
