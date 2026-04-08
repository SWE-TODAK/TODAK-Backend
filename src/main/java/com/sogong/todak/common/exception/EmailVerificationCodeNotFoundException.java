package com.sogong.todak.common.exception;

public class EmailVerificationCodeNotFoundException extends EmailVerificationException {

    public EmailVerificationCodeNotFoundException(String message) {
        super(message);
    }
}
