package com.todak.api.auth.service;

import com.todak.api.auth.dto.response.TokenResponse;
import com.todak.api.auth.jwt.JwtTokenProvider;
import com.todak.api.infra.kakao.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoAuthService kakaoAuthService;

    public TokenResponse hospitalLogin(String loginId, String password) {
        // 임시 로직: 아이디가 'admin'이고 비번이 '1234'면 로그인 성공
        if ("admin".equals(loginId) && "1234".equals(password)) {
            // 관리자 토큰 발급 (ID: 1, 역할: ADMIN)
            String accessToken = jwtTokenProvider.createToken(1L, "ADMIN");
            return new TokenResponse(accessToken, "refresh_token_dummy");
        }
        throw new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다.");
    }

    public TokenResponse kakaoLogin(String authCode) {
        // ... (기존 카카오 로그인 로직 유지)
        String kakaoAccessToken = kakaoAuthService.getAccessToken(authCode);
        Long kakaoUserId = kakaoAuthService.getKakaoUserId(kakaoAccessToken);
        String accessToken = jwtTokenProvider.createToken(kakaoUserId, "USER");
        return new TokenResponse(accessToken, "refresh_token_dummy");
    }
}