package com.sogong.todak.auth.oauth2.userinfo;

import java.util.Map;
import java.util.Optional;

public class KakaoUserInfo extends OAuth2UserInfo {

    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;

    @SuppressWarnings("unchecked")
    public KakaoUserInfo(Map<String, Object> attributes) {
        super(attributes);

        // 생성자에서 미리 하위 데이터 구조를 파싱하여 반복 탐색 방지
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

    @Override
    public String getProfileImageUrl() {
        if (profile == null) return null;

        // 우선순위에 따른 프로필 이미지 추출 (가장 고화질 -> 저화질 순 권장)
        return firstNonNull(
                profile.get("profile_image_url"),
                profile.get("profile_image"),
                profile.get("thumbnail_image_url"),
                profile.get("thumbnail_image")
        );
    }

    /**
     * 여러 개의 후보 중 가장 먼저 null이 아닌 값을 반환하는 헬퍼 메서드
     */
    private String firstNonNull(Object... values) {
        for (Object v : values) {
            if (v != null) return String.valueOf(v);
        }
        return null;
    }
}