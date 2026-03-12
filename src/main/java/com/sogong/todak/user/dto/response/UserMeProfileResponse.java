package com.sogong.todak.user.dto.response;

import com.sogong.todak.user.entity.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "프로필 설정 조회 응답")
public class UserMeProfileResponse {

    @Schema(description = "닉네임", example = "토닥이")
    private String nickname;

    @Schema(description = "이메일", example = "todak@example.com")
    private String email;

    @Schema(description = "생년월일", example = "1995-03-21")
    private LocalDate birthDate;

    @Schema(description = "성별", example = "MALE")
    private Gender gender;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.todak.com/profile/user-1.png")
    private String profileImageUrl;

    @Schema(description = "카카오 연동 여부", example = "true")
    private boolean kakaoLinked;

    @Schema(description = "로컬 비밀번호 설정 여부", example = "true")
    private boolean hasPassword;
}
