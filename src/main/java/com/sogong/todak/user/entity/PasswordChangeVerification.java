package com.sogong.todak.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "password_change_verifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordChangeVerification {

    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "code_hash", columnDefinition = "text", nullable = false)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "sent_at", nullable = false)
    private OffsetDateTime sentAt;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static PasswordChangeVerification issue(
            UUID userId,
            String email,
            String codeHash,
            OffsetDateTime sentAt,
            OffsetDateTime expiresAt
    ) {
        PasswordChangeVerification verification = new PasswordChangeVerification();
        verification.userId = userId;
        verification.email = email;
        verification.codeHash = codeHash;
        verification.sentAt = sentAt;
        verification.expiresAt = expiresAt;
        verification.failedAttempts = 0;
        verification.usedAt = null;
        return verification;
    }

    public void reissue(String email, String codeHash, OffsetDateTime sentAt, OffsetDateTime expiresAt) {
        this.email = email;
        this.codeHash = codeHash;
        this.sentAt = sentAt;
        this.expiresAt = expiresAt;
        this.usedAt = null;
        this.failedAttempts = 0;
    }

    public boolean isExpired(OffsetDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean canResend(OffsetDateTime now, Duration resendInterval) {
        return !sentAt.plus(resendInterval).isAfter(now);
    }

    public int increaseFailedAttempts() {
        failedAttempts += 1;
        return failedAttempts;
    }

    public void markUsed(OffsetDateTime now) {
        this.usedAt = now;
    }

    public void expireAt(OffsetDateTime now) {
        this.expiresAt = now;
    }
}
