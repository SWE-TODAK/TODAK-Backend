package com.todak.api.common.exception; // common 패키지 안의 exception 패키지

import com.todak.api.common.response.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonResponse<Void>> handleRuntimeException(RuntimeException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(500)
                .body(CommonResponse.error(500, e.getMessage()));
    }
}