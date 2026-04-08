package com.sogong.todak.common.exception;

public class EmailVerificationCodeExpiredException extends EmailVerificationException {

    public EmailVerificationCodeExpiredException(String message) {
        super(message);
    }
}
