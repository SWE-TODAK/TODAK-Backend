package com.sogong.todak.auth.oauth2.userinfo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

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

    // --- 추가된 생년월일 처리 로직 ---

    /**
     * 카카오의 birthyear(YYYY)와 birthday(MMDD)를 합쳐 LocalDate로 반환합니다.
     * DB의 birth_date (date) 컬럼과 매핑됩니다.
     */
    public LocalDate getBirthDate() {
        if (kakaoAccount == null) return null;

        String birthYear = (String) kakaoAccount.get("birthyear"); // 예: "1995"
        String birthDay = (String) kakaoAccount.get("birthday");   // 예: "0521"

        if (birthYear == null || birthDay == null) {
            return null;
        }

        try {
            // "1995" + "0521" = "19950521" -> LocalDate 변환
            return LocalDate.parse(birthYear + birthDay, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            // 날짜 파싱 실패 시 로그를 남기거나 null 반환
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