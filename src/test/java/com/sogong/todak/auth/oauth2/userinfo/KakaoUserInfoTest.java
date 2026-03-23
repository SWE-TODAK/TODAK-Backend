package com.sogong.todak.auth.oauth2.userinfo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KakaoUserInfoTest {

    @Test
    @DisplayName("birthyear와 birthday를 LocalDate로 정상 변환한다")
    void parseBirthDateSuccess() {
        LocalDate birthDate = KakaoUserInfo.parseBirthDate("2001", "0523");

        assertEquals(LocalDate.of(2001, 5, 23), birthDate);
    }

    @Test
    @DisplayName("birthday 또는 birthyear가 누락되면 null을 반환한다")
    void parseBirthDateReturnsNullWhenMissing() {
        assertNull(KakaoUserInfo.parseBirthDate("2001", null));
        assertNull(KakaoUserInfo.parseBirthDate(null, "0523"));
    }

    @Test
    @DisplayName("카카오 응답에서 birthDate를 추출한다")
    void extractBirthDateFromAttributes() {
        KakaoUserInfo userInfo = new KakaoUserInfo(Map.of(
                "id", 12345L,
                "kakao_account", Map.of(
                        "email", "kakao@example.com",
                        "birthyear", "2001",
                        "birthday", "0523",
                        "profile", Map.of(
                                "nickname", "토닥이",
                                "profile_image_url", "https://example.com/profile.png"
                        )
                )
        ));

        assertEquals(LocalDate.of(2001, 5, 23), userInfo.getBirthDate());
    }
}
