package com.sogong.todak.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private boolean isNewUser;
    private TokenPairResponse token;
    private UserSummaryResponse user;
}