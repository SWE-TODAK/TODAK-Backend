package com.sogong.todak.auth.refresh.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Getter
@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    /**
     * DB 저장용 해시값 (SHA-256 hex)
     * - raw token은 저장하지 않음
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    // ===============================
    // 발급용 팩토리
    // ===============================
    public static RefreshToken issue(UUID userId, String tokenHash, OffsetDateTime expiresAt) {
        RefreshToken rt = new RefreshToken();
        rt.userId = userId;
        rt.tokenHash = tokenHash;
        rt.expiresAt = expiresAt;
        rt.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        return rt;
    }

    // ===============================
    // 상태 체크
    // ===============================
    public boolean isExpired(OffsetDateTime now) {
        return expiresAt.isBefore(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void revoke(OffsetDateTime now) {
        this.revokedAt = now;
    }
}