package com.sogong.todak.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "회원 탈퇴 요청")
public class WithdrawRequest {

    @Schema(description = "LOCAL 사용자 비밀번호 재입력", example = "password1234", nullable = true)
    private String password;
}
