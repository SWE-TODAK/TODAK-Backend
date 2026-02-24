package com.sogong.todak.auth.oauth2.exchange;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * exchange code 저장소 추상화
 * - InMemory / Redis / DB 등 구현체 교체 가능
 * - issue: 코드 발급
 * - consume: 1회용 소비(성공 시 payload 반환, 실패 시 empty)
 */
public interface ExchangeCodeStore {
    String issue(UUID userId, boolean isNewUser);
    Optional<ExchangeCodePayload> consume(String code);

    void cleanup();

    Duration ttl();
}