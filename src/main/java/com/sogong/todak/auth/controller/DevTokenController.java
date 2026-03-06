package com.sogong.todak.auth.controller;

import com.sogong.todak.auth.jwt.JwtTokenProvider;
import com.sogong.todak.auth.refresh.service.RefreshTokenService;
import com.sogong.todak.user.repository.UserRepository;
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
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    /**
     * 개발용 토큰 발급 엔드포인트
     * @param userIdStr 테스트할 유저의 UUID (없으면 랜덤 생성)
     */
    @GetMapping("/dev-token")
    public ResponseEntity<Map<String, Object>> devToken(
            @RequestParam(name = "userId") String userIdStr
    ) {
        final UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Invalid UUID format. Please provide a valid UUID."
            ));
        }
        // ✅ 존재하지 않으면 FK로 insert가 터지므로 미리 400 처리
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "User not found. Please provide an existing userId."
            ));
        }

        // ✅ Access는 JWT
        String accessToken = jwtTokenProvider.createAccessToken(userId, "ROLE_USER");

        // ✅ Refresh는 DB(stateful)로 발급 (raw 반환)
        String refreshToken = refreshTokenService.issue(userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", userId);
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("tokenType", "Bearer");
        response.put("expiresInSeconds", jwtTokenProvider.getAccessExpiresInSeconds());
        response.put("rotated", true); // ✅ refresh는 새로 발급된 값이므로 교체 저장 신호

        response.put("context", "This token simulates a user regardless of UserAuth or UserIdentity storage.");
        response.put("usage", "Add 'Authorization: Bearer <accessToken>' to your request headers.");

        log.info("Development token issued for userId: {}", userId);

        return ResponseEntity.ok(response);
    }
}