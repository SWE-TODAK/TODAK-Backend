package com.sogong.todak.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordChangeCodeSendResponse {

    private String message;
    private String maskedEmail;
    private long expiresInSeconds;
    private long resendAvailableInSeconds;
}
