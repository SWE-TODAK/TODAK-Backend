package com.sogong.todak.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_auth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAuth {

    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // User의 PK를 그대로 자신의 PK로 사용
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "password_hash", columnDefinition = "text", nullable = false)
    private String passwordHash;

    @Column(name = "password_updated_at")
    private OffsetDateTime passwordUpdatedAt;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Builder
    public UserAuth(User user, String passwordHash) {
        this.user = user;
        this.passwordHash = passwordHash;
        this.passwordUpdatedAt = OffsetDateTime.now();
    }

    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.passwordUpdatedAt = OffsetDateTime.now();
    }

    public void updateLastLogin() {
        this.lastLoginAt = OffsetDateTime.now();
    }
}