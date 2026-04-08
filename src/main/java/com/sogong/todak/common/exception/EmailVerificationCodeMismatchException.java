package com.sogong.todak.common.exception;

public class EmailVerificationCodeMismatchException extends EmailVerificationException {

    public EmailVerificationCodeMismatchException(String message) {
        super(message);
    }
}
