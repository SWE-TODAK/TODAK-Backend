package com.sogong.todak.auth.oauth2.userinfo;

import java.util.Collections;
import java.util.Map;

/**
 * 소셜 로그인 제공자별 유저 정보를 공통화하기 위한 추상 클래스
 */
public abstract class OAuth2UserInfo {

    // 하위 클래스에서 읽기 전용으로 접근할 수 있도록 불변성 보장
    protected final Map<String, Object> attributes;

    protected OAuth2UserInfo(Map<String, Object> attributes) {
        // 데이터 오염 방지를 위해 불변 맵으로 복사하여 저장
        this.attributes = (attributes != null)
                ? Collections.unmodifiableMap(attributes)
                : Collections.emptyMap();
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /** provider의 유저 고유 식별자 (예: 카카오의 숫자 ID) */
    public abstract String getProviderId();

    /** provider에서 제공하는 이메일 (선택 사항) */
    public abstract String getEmail();

    /** provider에서 제공하는 닉네임 (선택 사항) */
    public abstract String getNickname();

    /** provider에서 제공하는 프로필 이미지 URL (선택 사항) */
    public abstract String getProfileImageUrl();

    /**
     * 맵에서 안전하게 문자열을 추출하기 위한 헬퍼 메서드
     */
    protected String getAttributeString(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);
        return (value != null) ? String.valueOf(value) : null;
    }
}