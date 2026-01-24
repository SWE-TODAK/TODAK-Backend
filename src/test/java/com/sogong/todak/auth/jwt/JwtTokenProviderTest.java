package com.sogong.todak.auth.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    // 테스트용 시크릿 키 (32바이트 이상)
    private final String secret = "test-secret-key-must-be-at-least-32-bytes-long-abcd";
    private final long expMin = 30;

    // 스프링 빈 주입 없이 직접 new로 생성 (순수 단위 테스트)
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(secret, expMin);

    @Test
    @DisplayName("액세스 토큰 생성 및 검증 성공 테스트")
    void createAndValidateToken() {
        // given
        Long userId = 1L;
        String role = "ROLE_USER";

        // when
        String token = jwtTokenProvider.createAccessToken(userId, role);

        // then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validate(token)).isTrue();
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getRole(token)).isEqualTo(role);
    }

    @Test
    @DisplayName("잘못된 토큰 검증 시 false 반환")
    void validateInvalidToken() {
        // given
        String invalidToken = "invalid.token.string";

        // when
        boolean isValid = jwtTokenProvider.validate(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }
}