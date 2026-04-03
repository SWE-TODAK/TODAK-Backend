package com.sogong.todak.user.controller;

import com.sogong.todak.user.dto.request.ChangePasswordRequest;
import com.sogong.todak.user.dto.request.UpdateBirthRequest;
import com.sogong.todak.user.dto.request.UpdateEmailRequest;
import com.sogong.todak.user.dto.request.UpdateGenderRequest;
import com.sogong.todak.user.dto.request.UpdateNicknameRequest;
import com.sogong.todak.user.dto.request.UpdateProfileImageRequest;
import com.sogong.todak.user.dto.response.PasswordChangeResponse;
import com.sogong.todak.user.dto.response.UserMeProfileResponse;
import com.sogong.todak.user.dto.response.UserMeResponse;
import com.sogong.todak.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "마이페이지 조회 API")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "마이페이지 기본 정보 조회",
            description = "더보기 메인 화면에서 사용하는 현재 로그인 사용자 기본 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserMeResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> getMyPage() {
        return ResponseEntity.ok(userService.getMyPage());
    }

    @Operation(
            summary = "프로필 설정 정보 조회",
            description = "프로필 설정 화면에서 사용하는 현재 로그인 사용자 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserMeProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me/profile")
    public ResponseEntity<UserMeProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @Operation(
            summary = "프로필 이미지 등록/수정",
            description = "현재 로그인 사용자의 프로필 이미지를 등록하거나 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "등록/수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/me/profile/image")
    public ResponseEntity<Void> updateProfileImage(@Valid @RequestBody UpdateProfileImageRequest request) {
        userService.updateProfileImage(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "프로필 이미지 삭제",
            description = "현재 로그인 사용자의 프로필 이미지를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @DeleteMapping("/me/profile/image")
    public ResponseEntity<Void> deleteProfileImage() {
        userService.deleteProfileImage();
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "닉네임 수정",
            description = "현재 로그인 사용자의 닉네임을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/me/profile/name")
    public ResponseEntity<Void> updateNickname(@Valid @RequestBody UpdateNicknameRequest request) {
        userService.updateNickname(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "이메일 수정",
            description = "현재 로그인 사용자의 이메일을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/me/profile/email")
    public ResponseEntity<Void> updateEmail(@Valid @RequestBody UpdateEmailRequest request) {
        userService.updateEmail(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "생년월일 수정",
            description = "현재 로그인 사용자의 생년월일을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/me/profile/birth")
    public ResponseEntity<Void> updateBirthDate(@Valid @RequestBody UpdateBirthRequest request) {
        userService.updateBirthDate(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "성별 수정",
            description = "현재 로그인 사용자의 성별을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/me/profile/sex")
    public ResponseEntity<Void> updateGender(@Valid @RequestBody UpdateGenderRequest request) {
        userService.updateGender(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "내 비밀번호 변경",
            description = "현재 로그인한 LOCAL 사용자의 비밀번호를 검증 후 변경하고, 기존 refresh token을 모두 무효화합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공",
                    content = @Content(schema = @Schema(implementation = PasswordChangeResponse.class))),
            @ApiResponse(responseCode = "400", description = "비밀번호 검증 실패 또는 LOCAL 계정 아님"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/me/password")
    public ResponseEntity<PasswordChangeResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(userService.changePassword(request));
    }
}
