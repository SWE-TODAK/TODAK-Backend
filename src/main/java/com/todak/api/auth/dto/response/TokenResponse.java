package com.todak.api.auth.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter @AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
}