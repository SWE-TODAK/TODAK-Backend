package com.sogong.todak.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private AuthResult authResult;
    private TokenPairResponse token;
    private UserSummaryResponse user;
}
