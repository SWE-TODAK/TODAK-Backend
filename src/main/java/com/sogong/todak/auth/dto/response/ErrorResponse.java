package com.sogong.todak.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;     // BAD_REQUEST, UNAUTHORIZED ...
    private String message;   // 사용자에게 보여줄 메시지
    private String path;      // 요청 URI
}