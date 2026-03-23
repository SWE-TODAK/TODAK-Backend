//이미 탈퇴했을 경우 예외처리 하기 위해
package com.sogong.todak.common.exception;

public class AlreadyWithdrawnUserException extends RuntimeException {

    public AlreadyWithdrawnUserException(String message) {
        super(message);
    }
}
