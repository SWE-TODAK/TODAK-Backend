package com.sogong.todak.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sogong.todak.common.exception.GlobalExceptionHandler;
import com.sogong.todak.user.dto.response.PasswordChangeCodeSendResponse;
import com.sogong.todak.user.dto.response.PasswordChangeResponse;
import com.sogong.todak.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);

        UserController controller = new UserController(userService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("프로필 이미지 수정 요청은 204를 반환한다")
    void updateProfileImageReturnsNoContent() throws Exception {
        Map<String, Object> request = Map.of(
                "profileImageUrl", "https://cdn.todak.com/profile/tester.png"
        );

        mockMvc.perform(patch("/api/v1/users/me/profile/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).updateProfileImage(any());
    }

    @Test
    @DisplayName("프로필 이미지 삭제 요청은 204를 반환한다")
    void deleteProfileImageReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me/profile/image"))
                .andExpect(status().isNoContent());

        verify(userService).deleteProfileImage();
    }

    @Test
    @DisplayName("비밀번호 변경 인증코드 발송 요청은 200을 반환한다")
    void sendPasswordChangeVerificationCodeReturnsOk() throws Exception {
        when(userService.sendPasswordChangeVerificationCode()).thenReturn(PasswordChangeCodeSendResponse.builder()
                .message("인증코드를 이메일로 발송했습니다.")
                .maskedEmail("loc***@example.com")
                .expiresInSeconds(300)
                .resendAvailableInSeconds(60)
                .build());

        mockMvc.perform(post("/api/v1/users/me/password/email/send-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("인증코드를 이메일로 발송했습니다."))
                .andExpect(jsonPath("$.maskedEmail").value("loc***@example.com"));

        verify(userService).sendPasswordChangeVerificationCode();
    }

    @Test
    @DisplayName("비밀번호 변경 요청은 200과 메시지를 반환한다")
    void changePasswordReturnsOk() throws Exception {
        when(userService.changePassword(any())).thenReturn(PasswordChangeResponse.builder()
                .message("비밀번호가 변경되었습니다. 다시 로그인해주세요.")
                .build());

        Map<String, Object> request = Map.of(
                "verificationCode", "123456",
                "currentPassword", "password1234",
                "newPassword", "new-password123",
                "confirmNewPassword", "new-password123"
        );

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 변경되었습니다. 다시 로그인해주세요."));

        verify(userService).changePassword(any());
    }

    @Test
    @DisplayName("비밀번호 변경 요청에 currentPassword가 없으면 400을 반환한다")
    void changePasswordWithoutCurrentPasswordReturnsBadRequest() throws Exception {
        Map<String, Object> request = Map.of(
                "verificationCode", "123456",
                "newPassword", "new-password123",
                "confirmNewPassword", "new-password123"
        );

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("현재 비밀번호는 필수입니다."));
    }

    @Test
    @DisplayName("비밀번호 변경 요청에 verificationCode가 없으면 400을 반환한다")
    void changePasswordWithoutVerificationCodeReturnsBadRequest() throws Exception {
        Map<String, Object> request = Map.of(
                "currentPassword", "password1234",
                "newPassword", "new-password123",
                "confirmNewPassword", "new-password123"
        );

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("인증코드는 필수입니다."));
    }
}
