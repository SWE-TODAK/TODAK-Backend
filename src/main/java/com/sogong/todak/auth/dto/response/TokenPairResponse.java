package com.sogong.todak.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenPairResponse {

    private String tokenType; // "Bearer"
    private String accessToken;
    private String refreshToken;
    private long expiresInSeconds;

}
