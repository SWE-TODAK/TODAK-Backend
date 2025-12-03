package com.todak.api.auth.controller;

import com.todak.api.auth.dto.request.HospitalLoginRequest;
import com.todak.api.auth.dto.request.KakaoLoginRequest;
import com.todak.api.auth.dto.response.TokenResponse;
import com.todak.api.auth.service.AuthService;
import com.todak.api.common.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @PostMapping("/auth/hospital/login")
    public CommonResponse<TokenResponse> hospitalLogin(@RequestBody HospitalLoginRequest request) {
        TokenResponse token = authService.hospitalLogin(request.getLoginId(), request.getPassword());
        return CommonResponse.success(token);
    }

    // 2. 카카오 로그인
    @PostMapping("/kakao/login")
    public CommonResponse<TokenResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        TokenResponse token = authService.kakaoLogin(request.getKakaoAccessKey());
        return CommonResponse.success(token);
    }
    @GetMapping("/oauth/callback/kakao")
    public void kakaoCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        // 1. 카카오가 준 인가 코드(code)를 받음
        // 2. 앱의 딥링크(todak://kakao-login) 뒤에 code를 붙여서 리다이렉트
        //    -> 앱이 켜지면서 이 code를 낚아채게 됨
        String appDeepLink = "todak://kakao-login?code=" + code;
        response.sendRedirect(appDeepLink);
    }


    // 3. 로그아웃
    @PostMapping("/auth/logout")
    public CommonResponse<Void> logout() {
        return CommonResponse.success();
    }
}