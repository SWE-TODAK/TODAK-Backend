package com.sogong.todak.common.exception;

public class EmailVerificationCodeAlreadyUsedException extends EmailVerificationException {

    public EmailVerificationCodeAlreadyUsedException(String message) {
        super(message);
    }
}
