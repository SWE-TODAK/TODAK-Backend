package com.sogong.todak.auth.controller;

import com.sogong.todak.auth.dto.request.LoginRequest;
import com.sogong.todak.auth.dto.request.OAuthExchangeRequest;
import com.sogong.todak.auth.dto.request.SignupRequest;
import com.sogong.todak.auth.dto.request.RefreshRequest;
import com.sogong.todak.auth.dto.request.LogoutRequest;
import com.sogong.todak.auth.dto.response.AuthResponse;
import com.sogong.todak.auth.dto.response.TokenPairResponse;
import com.sogong.todak.auth.jwt.JwtTokenProvider;
import com.sogong.todak.auth.oauth2.service.LocalAuthService;
import com.sogong.todak.auth.oauth2.service.OAuthExchangeService;
import com.sogong.todak.auth.refresh.service.RefreshTokenService;
import com.sogong.todak.auth.refresh.service.RotateResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LocalAuthService localAuthService;
    private final OAuthExchangeService oAuthExchangeService;

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/oauth/exchange")
    public ResponseEntity<AuthResponse> oauthExchange(@Valid @RequestBody OAuthExchangeRequest request) {
        return ResponseEntity.ok(oAuthExchangeService.exchange(request));
    }

    @Value("${app.oauth2.kakao.authorization-uri:/oauth2/authorization/kakao}")
    private String kakaoAuthorizationUri;

    // =========================
    // Kakao
    // =========================

    @PostMapping("/kakao/login")
    public ResponseEntity<Map<String, String>> kakaoLogin() {
        return ResponseEntity.ok(Map.of("redirectUrl", kakaoAuthorizationUri));
    }

    @PostMapping("/kakao/signup")
    public ResponseEntity<?> kakaoSignup(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "message", "NOT_IMPLEMENTED: kakao signup flow (additional profile info) is not wired yet.",
                "request", body
        ));
    }

    @PostMapping("/kakao/link")
    public ResponseEntity<?> kakaoLink() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "message", "NOT_IMPLEMENTED: kakao link flow is not wired yet."
        ));
    }

    @DeleteMapping("/kakao/unlink")
    public ResponseEntity<?> kakaoUnlink() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "message", "NOT_IMPLEMENTED: kakao unlink flow is not wired yet."
        ));
    }

    // =========================
    // Local
    // =========================
    @PostMapping("/local/login")
    public ResponseEntity<AuthResponse> localLogin(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = localAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/local/signup")
    public ResponseEntity<AuthResponse> localSignup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = localAuthService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================
    // Token / Account
    // =========================

    /**
     * ✅ 토큰 재발급
     * POST /api/v1/auth/token/refresh
     * body: { "refreshToken": "raw..." }
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<TokenPairResponse> refreshToken(@Valid @RequestBody RefreshRequest request) {

        RotateResult rotated = refreshTokenService.rotate(request.getRefreshToken());

        UUID userId = rotated.userId();
        String newAccess = jwtTokenProvider.createAccessToken(userId, "ROLE_USER");

        TokenPairResponse resp = TokenPairResponse.builder()
                .tokenType("Bearer")
                .accessToken(newAccess)
                .refreshToken(rotated.newRefreshToken()) // 새 refresh raw
                .expiresInSeconds(jwtTokenProvider.getAccessExpiresInSeconds())
                .rotated(true) // ✅ 반드시 교체 저장하라는 신호
                .build();

        return ResponseEntity.ok(resp);
    }

    /**
     * ✅ 로그아웃(이 기기 한정）
     * POST /api/v1/auth/logout
     * body: { "refreshToken": "raw..." }
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @GetMapping("/providers")
    public ResponseEntity<?> providers() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "message", "NOT_IMPLEMENTED: providers query is not wired yet."
        ));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "message", "NOT_IMPLEMENTED: change password is not wired yet.",
                "request", body
        ));
    }
}