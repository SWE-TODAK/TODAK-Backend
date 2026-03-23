package com.sogong.todak.auth.oauth2.userinfo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class OAuthUserProfile {

    private final String email;
    private final String nickname;
    private final String profileImageUrl;
    private final LocalDate birthDate;
}
