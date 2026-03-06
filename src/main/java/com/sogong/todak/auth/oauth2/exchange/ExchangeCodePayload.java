package com.sogong.todak.auth.oauth2.exchange;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * exchangeCode에 매핑되는 payload
 * - userId: 누구의 코드인지
 * - isNewUser: 신규 가입 여부(=온보딩 필요 여부로 쓰는지 정책 합의 필요)
 * - expiresAt: 만료 시간
 */
public record ExchangeCodePayload(
        UUID userId,
        boolean isNewUser,
        Instant expiresAt
) {
    public ExchangeCodePayload {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    public boolean isExpired(Instant now) {
        // 만료시각 포함(<=) 정책
        return !expiresAt.isAfter(now);
    }
}