package com.sogong.todak.user.service.impl;

import com.sogong.todak.auth.refresh.service.RefreshTokenService;
import com.sogong.todak.common.exception.InvalidPasswordException;
import com.sogong.todak.common.exception.UnsupportedAuthProviderException;
import com.sogong.todak.user.dto.request.ChangePasswordRequest;
import com.sogong.todak.user.dto.response.PasswordChangeCodeSendResponse;
import com.sogong.todak.user.dto.response.PasswordChangeResponse;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.entity.UserAuth;
import com.sogong.todak.user.repository.UserRepository;
import com.sogong.todak.user.service.PasswordChangeVerificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordChangeVerificationService passwordChangeVerificationService;

    @InjectMocks
    private UserServiceImpl userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("LOCAL 유저는 현재 비밀번호가 맞으면 비밀번호 변경에 성공하고 refresh token을 제거한다")
    void changePasswordSuccess() {
        UUID userId = UUID.randomUUID();
        User user = localUser(userId, "encoded-current");
        ChangePasswordRequest request = changePasswordRequest("123456", "current-password", "new-password123", "new-password123");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-password", "encoded-current")).thenReturn(true);
        when(passwordEncoder.matches("new-password123", "encoded-current")).thenReturn(false);
        when(passwordEncoder.encode("new-password123")).thenReturn("encoded-new");

        PasswordChangeResponse response = userService.changePassword(request);

        assertEquals("비밀번호가 변경되었습니다. 다시 로그인해주세요.", response.getMessage());
        assertEquals("encoded-new", user.getAuth().getPasswordHash());
        verify(passwordChangeVerificationService).verifyCode(user, "123456");
        verify(passwordChangeVerificationService).consumeCode(userId);
        verify(refreshTokenService).removeAllByUserId(userId);
    }

    @Test
    @DisplayName("LOCAL auth가 없는 계정은 비밀번호를 변경할 수 없다")
    void changePasswordFailsWhenLocalAuthMissing() {
        UUID userId = UUID.randomUUID();
        User user = userWithoutLocalAuth(userId);
        ChangePasswordRequest request = changePasswordRequest("123456", "current-password", "new-password123", "new-password123");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        assertThrows(UnsupportedAuthProviderException.class, () -> userService.changePassword(request));

        verify(passwordChangeVerificationService, never()).verifyCode(user, "123456");
        verify(refreshTokenService, never()).removeAllByUserId(userId);
    }

    @Test
    @DisplayName("현재 비밀번호가 일치하지 않으면 비밀번호 변경에 실패한다")
    void changePasswordFailsWhenCurrentPasswordMismatch() {
        UUID userId = UUID.randomUUID();
        User user = localUser(userId, "encoded-current");
        ChangePasswordRequest request = changePasswordRequest("123456", "wrong-password", "new-password123", "new-password123");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-current")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> userService.changePassword(request));

        verify(passwordChangeVerificationService).verifyCode(user, "123456");
        verify(refreshTokenService, never()).removeAllByUserId(userId);
    }

    @Test
    @DisplayName("새 비밀번호 확인이 다르면 비밀번호 변경에 실패한다")
    void changePasswordFailsWhenConfirmationMismatch() {
        UUID userId = UUID.randomUUID();
        User user = localUser(userId, "encoded-current");
        ChangePasswordRequest request = changePasswordRequest("123456", "current-password", "new-password123", "different-password");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-password", "encoded-current")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(request));

        verify(passwordChangeVerificationService).verifyCode(user, "123456");
        verify(refreshTokenService, never()).removeAllByUserId(userId);
    }

    @Test
    @DisplayName("새 비밀번호는 현재 비밀번호와 같을 수 없다")
    void changePasswordFailsWhenNewPasswordMatchesCurrentPassword() {
        UUID userId = UUID.randomUUID();
        User user = localUser(userId, "encoded-current");
        ChangePasswordRequest request = changePasswordRequest("123456", "same-password", "same-password", "same-password");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("same-password", "encoded-current")).thenReturn(true);

        assertThrows(InvalidPasswordException.class, () -> userService.changePassword(request));

        verify(passwordChangeVerificationService).verifyCode(user, "123456");
        verify(refreshTokenService, never()).removeAllByUserId(userId);
    }

    @Test
    @DisplayName("비밀번호 변경 인증코드 발송은 현재 사용자 이메일 기준으로 처리한다")
    void sendPasswordChangeVerificationCodeSuccess() {
        UUID userId = UUID.randomUUID();
        User user = localUser(userId, "encoded-current");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(passwordChangeVerificationService.sendCode(user)).thenReturn(PasswordChangeCodeSendResponse.builder()
                .message("인증코드를 이메일로 발송했습니다.")
                .maskedEmail("loc***@example.com")
                .expiresInSeconds(300)
                .resendAvailableInSeconds(60)
                .build());

        PasswordChangeCodeSendResponse response = userService.sendPasswordChangeVerificationCode();

        assertEquals("인증코드를 이메일로 발송했습니다.", response.getMessage());
        verify(passwordChangeVerificationService).sendCode(user);
    }

    private void authenticate(UUID userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null)
        );
    }

    private ChangePasswordRequest changePasswordRequest(
            String verificationCode,
            String currentPassword,
            String newPassword,
            String confirmNewPassword
    ) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        ReflectionTestUtils.setField(request, "verificationCode", verificationCode);
        ReflectionTestUtils.setField(request, "currentPassword", currentPassword);
        ReflectionTestUtils.setField(request, "newPassword", newPassword);
        ReflectionTestUtils.setField(request, "confirmNewPassword", confirmNewPassword);
        return request;
    }

    private User localUser(UUID userId, String passwordHash) {
        User user = User.builder()
                .email("local@example.com")
                .nickname("local-user")
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);
        ReflectionTestUtils.setField(user, "identities", new ArrayList<>());

        UserAuth userAuth = UserAuth.builder()
                .user(user)
                .passwordHash(passwordHash)
                .build();
        ReflectionTestUtils.setField(user, "auth", userAuth);

        return user;
    }

    private User userWithoutLocalAuth(UUID userId) {
        User user = User.builder()
                .email("kakao@example.com")
                .nickname("kakao-user")
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);
        ReflectionTestUtils.setField(user, "identities", new ArrayList<>());
        ReflectionTestUtils.setField(user, "auth", null);
        return user;
    }
}
