package com.sogong.todak.auth.oauth2.service;

import lombok.Getter;

@Getter
public class AuthExchangeException extends RuntimeException {

    private final AuthExchangeError error;

    public AuthExchangeException(AuthExchangeError error) {
        super(error.name());
        this.error = error;
    }

    public AuthExchangeException(AuthExchangeError error, String message) {
        super(message);
        this.error = error;
    }

    public AuthExchangeException(AuthExchangeError error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }
}