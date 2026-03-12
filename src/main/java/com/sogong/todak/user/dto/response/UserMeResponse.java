package com.sogong.todak.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "마이페이지 기본 정보 응답")
public class UserMeResponse {

    @Schema(description = "닉네임", example = "토닥이")
    private String nickname;

    @Schema(description = "이메일", example = "todak@example.com")
    private String email;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.todak.com/profile/user-1.png")
    private String profileImageUrl;
}
