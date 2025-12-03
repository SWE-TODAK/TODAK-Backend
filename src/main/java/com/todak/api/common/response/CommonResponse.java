package com.todak.api.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommonResponse<T> {
    private int status;
    private String message;
    private T data;

    // 데이터가 있는 성공 응답
    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(200, "요청 처리에 성공했습니다.", data);
    }

    // ★ 데이터가 없는 성공 응답
    public static CommonResponse<Void> success() {
        return new CommonResponse<>(200, "요청 처리에 성공했습니다.", null);
    }

    // 실패 응답
    public static CommonResponse<Void> error(int status, String message) {
        return new CommonResponse<>(status, message, null);
    }
}