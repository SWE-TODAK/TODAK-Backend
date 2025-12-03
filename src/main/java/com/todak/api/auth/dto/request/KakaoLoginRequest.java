package com.todak.api.auth.dto.request;
import lombok.Getter;
@Getter
public class KakaoLoginRequest {
    private String kakaoAccessKey; // 프론트에서 받은 카카오 토큰
}