package com.sogong.todak.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenPairResponse {

    private String tokenType; // "Bearer"
    private String accessToken;

    /**
     * RTR 사용 시 refresh는 회전
     * rotated=true면 클라이언트는 refreshToken을 반드시 교체 저장
     */
    private String refreshToken;

    private long expiresInSeconds;

    /**
     * refresh token이 이번 응답에서 교체(rotate)되었는지 여부
     * - login/signup: 보통 true (새로 발급)
     * - refresh: true (RTR 적용 시)
     */
    private boolean rotated;
}