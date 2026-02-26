package com.sogong.todak.auth.controller;

import com.sogong.todak.auth.dto.request.LoginRequest;
import com.sogong.todak.auth.dto.request.OAuthExchangeRequest;
import com.sogong.todak.auth.dto.request.SignupRequest;
import com.sogong.todak.auth.dto.response.AuthResponse;
import com.sogong.todak.auth.oauth2.service.LocalAuthService;
import com.sogong.todak.auth.oauth2.service.OAuthExchangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LocalAuthService localAuthService;
    private final OAuthExchangeService oAuthExchangeService;

    @PostMapping("/oauth/exchange")
    public ResponseEntity<AuthResponse> oauthExchange(@Valid @RequestBody OAuthExchangeRequest request) {
        return ResponseEntity.ok(oAuthExchangeService.exchange(request));
    }
    /**
     * 프론트/앱이 열 "카카오 로그인 시작 URL"
     * - 로컬:  http://localhost:8080/oauth2/authorization/kakao
     * - 운영:  https://todak.com/oauth2/authorization/kakao (리버스프록시 구성에 따라)
     *
     *  - SuccessHandler가 todak://auth/callback?code=XXXX 로 딥링크 리다이렉트하게 될 예정
     */
    @Value("${app.oauth2.kakao.authorization-uri:/oauth2/authorization/kakao}")
    private String kakaoAuthorizationUri;

    // =========================
    // Kakao
    // =========================

    /**
     * ✅ 카카오 로그인 시작 (명세: POST /auth/kakao/login)
     * - 앱이 redirectUrl을 받아서 브라우저/웹뷰로 열어주는 방식
     */
    @PostMapping("/kakao/login")
    public ResponseEntity<Map<String, String>> kakaoLogin() {
        return ResponseEntity.ok(Map.of("redirectUrl", kakaoAuthorizationUri));
    }

    /**
     * ✅ 카카오 회원가입 (명세: POST /auth/kakao/signup)
     * - 권장 의미: "카카오 로그인 후 신규 유저가 추가 정보 입력 완료" 같은 용도로 사용
     * - 현재는 스켈레톤 (추가정보 DTO 확정 후 서비스 연결)
     */
    @PostMapping("/kakao/signup")
    public ResponseEntity<?> kakaoSignup(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "message", "NOT_IMPLEMENTED: kakao signup flow (additional profile info) is not wired yet.",
                "request", body
        ));
    }

    /**
     * ✅ 카카오 계정 연결 (명세: POST /auth/kakao/link)
     * - 보통: 로그인된 상태에서 "연결 모드"로 카카오 인증을 시작시키는 endpoint
     * - 현재는 스켈레톤 (mode=link를 붙여 redirectUrl 내려주는 방식으로 구현 예정)
     */
    @PostMapping("/kakao/link")
    public ResponseEntity<?> kakaoLink() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "message", "NOT_IMPLEMENTED: kakao link flow is not wired yet."
        ));
    }

    /**
     * ✅ 카카오 계정 연결 해제 (명세: DELETE /auth/kakao/unlink)
     * - 로그인된 유저의 KAKAO identity row 삭제(또는 revoked) 처리
     */
    @DeleteMapping("/kakao/unlink")
    public ResponseEntity<?> kakaoUnlink() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "message", "NOT_IMPLEMENTED: kakao unlink flow is not wired yet."
        ));
    }

    // =========================
    // Local
    // =========================

    /**
     * ✅ 자체 로그인 (명세: POST /auth/local/login)
     */
    @PostMapping("/local/login")
    public ResponseEntity<AuthResponse> localLogin(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = localAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ 자체 회원가입 (명세: POST /auth/local/signup)
     */
    @PostMapping("/local/signup")
    public ResponseEntity<AuthResponse> localSignup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = localAuthService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================
    // Token / Account
    // =========================

    /**
     * ✅ 토큰 재발급 (명세: POST /auth/token/refresh)
     * - 모바일에서는 refreshToken을 body로 받는 방식이 일반적
     * - (예) { "refreshToken": "..." }
     * - 현재는 스켈레톤 (Refresh 로직 + DTO 확정 후 연결)
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "message", "NOT_IMPLEMENTED: token refresh is not wired yet.",
                "request", body
        ));
    }

    /**
     * ✅ 인증수단 확인 (명세: GET /auth/providers)
     * - 응답 예: { "providers": ["LOCAL", "KAKAO"] }
     * - 현재는 스켈레톤 (로그인 필요 endpoint로 동작시키는 걸 권장)
     */
    @GetMapping("/providers")
    public ResponseEntity<?> providers() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "message", "NOT_IMPLEMENTED: providers query is not wired yet."
        ));
    }

    /**
     * ✅ 비밀번호 변경 (명세: PUT /auth/password)
     * - (예) { "currentPassword": "...", "newPassword": "..." }
     * - 현재는 스켈레톤 (DTO/Service 확정 후 연결)
     */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "message", "NOT_IMPLEMENTED: change password is not wired yet.",
                "request", body
        ));
    }
}