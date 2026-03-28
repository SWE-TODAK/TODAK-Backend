package com.sogong.todak.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sogong.todak.auth.oauth2.userinfo.OAuthUserProfile;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "email", length = 255)
    private String email;

    // DB 컬럼명이 'name'이므로 매핑을 명시하거나 DB를 nickname으로 변경해야 합니다.
    @Column(name = "nickname", length = 50, nullable = false)
    private String nickname;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "profile_image_url", columnDefinition = "text")
    private String profileImageUrl;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    // 자체 로그인 정보 (1:1)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserAuth auth;

    // 소셜 로그인 정보들 (1:N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserIdentity> identities = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Builder
    public User(String email, String nickname, LocalDate birthDate, Gender gender, String profileImageUrl) {
        this.email = email;
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.gender = gender;
        this.profileImageUrl = profileImageUrl;
    }

    public void syncOAuth2Profile(String email, String nickname, String profileImageUrl, LocalDate birthDate) { // 파라미터 4개 확인
        applyOAuth2Profile(OAuthUserProfile.builder()
                .email(email)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .birthDate(birthDate)
                .build());
    }

    public void applyOAuth2Profile(OAuthUserProfile profile) {
        if (profile == null) {
            return;
        }

        if (profile.getEmail() != null) {
            this.email = profile.getEmail();
        }
        if (profile.getNickname() != null) {
            this.nickname = profile.getNickname();
        }
        if (profile.getProfileImageUrl() != null) {
            this.profileImageUrl = profile.getProfileImageUrl();
        }
        if (this.birthDate == null && profile.getBirthDate() != null) {
            this.birthDate = profile.getBirthDate();
        }
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void removeProfileImageUrl() {
        this.profileImageUrl = null;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void updateGender(Gender gender) {
        this.gender = gender;
    }

    public void restore() {
        this.deletedAt = null;
    }

    public void updateLocalProfile(String email, String nickname, LocalDate birthDate, Gender gender) {
        this.email = email;
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public void replaceWithLocalProfile(String email, String nickname, LocalDate birthDate, Gender gender) {
        this.email = email;
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.gender = gender;
        this.profileImageUrl = null;
    }

    public void replaceWithOAuth2Profile(OAuthUserProfile profile) {
        if (profile == null) {
            return;
        }

        this.email = profile.getEmail();
        this.nickname = profile.getNickname();
        this.profileImageUrl = profile.getProfileImageUrl();
        this.birthDate = profile.getBirthDate();
        this.gender = null;
    }

    public void clearAuth() {
        this.auth = null;
    }

    public boolean hasAuth() {
        return auth != null;
    }

    public boolean hasIdentity(com.sogong.todak.auth.domain.AuthProvider provider) {
        return identities.stream().anyMatch(identity -> identity.getProvider() == provider);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }
}
