package com.sogong.todak.auth.oauth2.userinfo;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class KakaoUserInfo extends OAuth2UserInfo {

    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;

    @SuppressWarnings("unchecked")
    public KakaoUserInfo(Map<String, Object> attributes) {
        super(attributes);
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (this.kakaoAccount != null) {
            this.profile = (Map<String, Object>) this.kakaoAccount.get("profile");
        } else {
            this.profile = null;
        }
    }

    @Override
    public String getProviderId() {
        return Optional.ofNullable(attributes.get("id"))
                .map(String::valueOf)
                .orElse(null);
    }

    @Override
    public String getEmail() {
        if (kakaoAccount == null) return null;
        return Optional.ofNullable(kakaoAccount.get("email"))
                .map(String::valueOf)
                .orElse(null);
    }

    @Override
    public String getNickname() {
        if (profile == null) return null;
        return Optional.ofNullable(profile.get("nickname"))
                .map(String::valueOf)
                .orElse(null);
    }

    public LocalDate getBirthDate() {
        return parseBirthDate(getBirthYear(), getBirthday());
    }

    public String getBirthYear() {
        return getAttributeString(kakaoAccount, "birthyear");
    }

    public String getBirthday() {
        return getAttributeString(kakaoAccount, "birthday");
    }

    public OAuthUserProfile toUserProfile() {
        return OAuthUserProfile.builder()
                .email(getEmail())
                .nickname(getNickname())
                .profileImageUrl(getProfileImageUrl())
                .birthDate(getBirthDate())
                .build();
    }

    public static LocalDate parseBirthDate(String birthYear, String birthday) {
        if (birthYear == null || birthday == null) {
            return null;
        }

        String normalizedBirthYear = birthYear.trim();
        String normalizedBirthday = birthday.trim();

        if (!normalizedBirthYear.matches("\\d{4}") || !normalizedBirthday.matches("\\d{4}")) {
            log.warn("Kakao birthDate format is invalid. birthyear={}, birthday={}", birthYear, birthday);
            return null;
        }

        try {
            return LocalDate.parse(normalizedBirthYear + normalizedBirthday, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse Kakao birthDate. birthyear={}, birthday={}", birthYear, birthday);
            return null;
        }
    }

    @Override
    public String getProfileImageUrl() {
        if (profile == null) return null;
        return firstNonNull(
                profile.get("profile_image_url"),
                profile.get("profile_image"),
                profile.get("thumbnail_image_url"),
                profile.get("thumbnail_image")
        );
    }

    private String firstNonNull(Object... values) {
        for (Object v : values) {
            if (v != null) return String.valueOf(v);
        }
        return null;
    }
}
