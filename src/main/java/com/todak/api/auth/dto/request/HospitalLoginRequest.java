package com.todak.api.auth.dto.request;

import lombok.Getter; // 이 import가 있어야 함
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HospitalLoginRequest {
    private String loginId;
    private String password;
}