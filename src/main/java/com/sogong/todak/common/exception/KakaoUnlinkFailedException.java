package com.sogong.todak.common.exception;

public class KakaoUnlinkFailedException extends RuntimeException {

    public KakaoUnlinkFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public KakaoUnlinkFailedException(String message) {
        super(message);
    }
}
