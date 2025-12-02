package com.todak.api.auth.service;

import com.todak.api.auth.dto.response.TokenResponse;
import com.todak.api.auth.jwt.JwtTokenProvider;
import com.todak.api.infra.kakao.KakaoAuthService;
import com.todak.api.user.entity.User;
import com.todak.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoAuthService kakaoAuthService;
    private final UserRepository userRepository;

    // 병원(관리자) 로그인
    public TokenResponse hospitalLogin(String loginId, String password) {
        if ("admin".equals(loginId) && "1234".equals(password)) {
            String accessToken = jwtTokenProvider.createToken(1L, "ADMIN");
            return new TokenResponse(accessToken, "refresh_token_dummy");
        }
        throw new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다.");
    }

    /**
     * 카카오 로그인 (앱에서 받은 AccessToken으로 로그인 처리)
     */
    @Transactional
    public TokenResponse kakaoLogin(String kakaoAccessToken) {

        // 1. 토큰이 비어있는지 확인 (안전장치)
        if (kakaoAccessToken == null || kakaoAccessToken.isEmpty()) {
            throw new RuntimeException("프론트엔드로부터 전달받은 카카오 토큰이 비어있습니다.");
        }

        Long kakaoUserId = kakaoAuthService.getKakaoUserId(kakaoAccessToken);

        User user = userRepository.findByKakaoId(kakaoUserId)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .kakaoId(kakaoUserId)
                                .role("USER")    // 기본 권한 설정
                                .build()
                ));

        String accessToken = jwtTokenProvider.createToken(kakaoUserId, "USER");

        return new TokenResponse(accessToken, "refresh_token_dummy");
    }
}