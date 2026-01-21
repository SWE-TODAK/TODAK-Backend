package com.sogong.todak.auth.controller;

import com.sogong.todak.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Profile("dev") // dev 프로파일에서만 활성화 (prod에서 절대 로딩 X)
public class DevTokenController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/dev-token")
    public ResponseEntity<Map<String, Object>> devToken(
            @RequestParam(defaultValue = "1") Long userId
    ) {
        if (userId == null || userId < 1) {
            return ResponseEntity.badRequest().body(Map.of("message", "userId must be >= 1"));
        }

        String token = jwtTokenProvider.createAccessToken(userId, "ROLE_USER");

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "accessToken", token,
                "tokenType", "Bearer"
        ));
    }
}
