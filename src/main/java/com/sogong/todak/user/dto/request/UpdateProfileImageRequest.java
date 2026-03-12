package com.sogong.todak.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateProfileImageRequest {

    @NotBlank(message = "프로필 이미지 URL은 필수입니다.")
    private String profileImageUrl;
}
