//회원탈퇴 중 토큰/인증 데이터 정리 실패를 감지해서 전체 탈퇴를 막기 위한 내부 서버 예외
package com.sogong.todak.common.exception;

public class AuthenticationDataCleanupException extends RuntimeException {

    public AuthenticationDataCleanupException(String message, Throwable cause) {
        super(message, cause);
    }
}
