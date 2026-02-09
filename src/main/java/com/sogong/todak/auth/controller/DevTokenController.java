package com.sogong.todak.auth.controller;

import com.sogong.todak.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Profile("dev") // 보안을 위해 dev 프로파일에서만 활성화
public class DevTokenController {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 개발용 토큰 발급 엔드포인트
     * @param userIdStr 테스트할 유저의 UUID (없으면 랜덤 생성)
     */
    @GetMapping("/dev-token")
    public ResponseEntity<Map<String, Object>> devToken(
            @RequestParam(name = "userId", required = false) String userIdStr
    ) {
        final UUID userId;
        try {
            // 1. ID 타입 일치화: 입력이 없으면 임시 UUID 생성, 있으면 파싱
            userId = (userIdStr == null || userIdStr.isBlank())
                    ? UUID.randomUUID()
                    : UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid UUID format. Please provide a valid UUID."));
        }

        // 2. 토큰 생성: 구조가 분리되었어도 토큰은 동일한 userId(UUID)를 주체(Subject)로 가짐
        String accessToken = jwtTokenProvider.createAccessToken(userId, "ROLE_USER");
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        // 3. 응답 구성: 테스트 가이드 메시지 추가
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", userId);
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("tokenType", "Bearer");
        response.put("context", "This token simulates a user regardless of UserAuth or UserIdentity storage.");
        response.put("usage", "Add 'Authorization: Bearer <accessToken>' to your request headers.");

        log.info("Development token issued for userId: {}", userId);

        return ResponseEntity.ok(response);
    }
}