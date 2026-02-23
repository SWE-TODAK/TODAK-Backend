package com.sogong.todak.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class OAuthExchangeRequest {

    @NotBlank(message = "교환 코드가 필요합니다.")
    private String code;
}