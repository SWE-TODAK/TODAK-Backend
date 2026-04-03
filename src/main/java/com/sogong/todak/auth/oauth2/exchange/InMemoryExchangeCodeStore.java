package com.sogong.todak.auth.oauth2.exchange;

import com.sogong.todak.auth.dto.response.AuthResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryExchangeCodeStore implements ExchangeCodeStore {

    private final Map<String, ExchangeCodePayload> store = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Duration ttl;

    public InMemoryExchangeCodeStore(
            @Value("${app.oauth2.exchange-code-ttl-seconds:300}") long ttlSeconds
    ) {
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    @Override
    public Duration ttl() {
        return ttl;
    }

    @Override
    public String issue(UUID userId, AuthResult authResult) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(authResult, "authResult must not be null");

        String code = generateCode();
        Instant now = Instant.now();
        ExchangeCodePayload payload = new ExchangeCodePayload(userId, authResult, now.plus(ttl));

        while (store.putIfAbsent(code, payload) != null) {
            code = generateCode();
        }

        log.info("[ExchangeCode] issued code={}, userId={}, authResult={}, exp={}",
                mask(code), userId, authResult, payload.expiresAt());
        return code;
    }

    @Override
    public Optional<ExchangeCodePayload> consume(String code) {
        if (code == null || code.isBlank()) return Optional.empty();

        ExchangeCodePayload payload = store.remove(code);
        if (payload == null) return Optional.empty();

        Instant now = Instant.now();
        if (payload.isExpired(now)) {
            log.info("[ExchangeCode] expired code={} userId={}", mask(code), payload.userId());
            return Optional.empty();
        }

        log.info("[ExchangeCode] consumed code={} userId={}", mask(code), payload.userId());
        return Optional.of(payload);
    }

    @Override
    public void cleanup() {
        Instant now = Instant.now();
        int before = store.size();
        store.entrySet().removeIf(e -> e.getValue().isExpired(now));
        int after = store.size();
        if (before != after) {
            log.debug("[ExchangeCode] cleanup removed {} entries", (before - after));
        }
    }

    @Scheduled(fixedDelayString = "${app.oauth2.exchange-code-cleanup-ms:60000}")
    public void scheduledCleanup() {
        cleanup();
    }

    private String generateCode() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String mask(String code) {
        if (code == null || code.length() < 8) return "****";
        return code.substring(0, 8) + "****";
    }
}
