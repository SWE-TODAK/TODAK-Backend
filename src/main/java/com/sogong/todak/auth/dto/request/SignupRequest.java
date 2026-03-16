package com.sogong.todak.auth.dto.request;

import com.sogong.todak.user.entity.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SignupRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 50)
    private String nickname;

    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate birthDate;

    private Gender gender;

    @Size(max = 2048, message = "프로필 이미지 URL은 2048자 이하여야 합니다.")
    private String profileImageUrl;
}
