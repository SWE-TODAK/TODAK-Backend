package com.sogong.todak.user.service.impl;

import com.sogong.todak.auth.refresh.service.RefreshTokenService;
import com.sogong.todak.common.exception.InvalidPasswordException;
import com.sogong.todak.common.exception.UnsupportedAuthProviderException;
import com.sogong.todak.user.dto.request.ChangePasswordRequest;
import com.sogong.todak.user.dto.response.PasswordChangeResponse;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.entity.UserAuth;
import com.sogong.todak.user.repository.UserRepository;
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
        ChangePasswordRequest request = changePasswordRequest("current-password", "new-password123", "new-password123");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-password", "encoded-current")).thenReturn(true);
        when(passwordEncoder.encode("new-password123")).thenReturn("encoded-new");

        PasswordChangeResponse response = userService.changePassword(request);

        assertEquals("비밀번호가 변경되었습니다. 다시 로그인해주세요.", response.getMessage());
        assertEquals("encoded-new", user.getAuth().getPasswordHash());
        verify(refreshTokenService).removeAllByUserId(userId);
    }

    @Test
    @DisplayName("LOCAL auth가 없는 계정은 비밀번호를 변경할 수 없다")
    void changePasswordFailsWhenLocalAuthMissing() {
        UUID userId = UUID.randomUUID();
        User user = userWithoutLocalAuth(userId);
        ChangePasswordRequest request = changePasswordRequest("current-password", "new-password123", "new-password123");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        assertThrows(UnsupportedAuthProviderException.class, () -> userService.changePassword(request));

        verify(refreshTokenService, never()).removeAllByUserId(userId);
    }

    @Test
    @DisplayName("현재 비밀번호가 일치하지 않으면 비밀번호 변경에 실패한다")
    void changePasswordFailsWhenCurrentPasswordMismatch() {
        UUID userId = UUID.randomUUID();
        User user = localUser(userId, "encoded-current");
        ChangePasswordRequest request = changePasswordRequest("wrong-password", "new-password123", "new-password123");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-current")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> userService.changePassword(request));

        verify(refreshTokenService, never()).removeAllByUserId(userId);
    }

    @Test
    @DisplayName("새 비밀번호 확인이 다르면 비밀번호 변경에 실패한다")
    void changePasswordFailsWhenConfirmationMismatch() {
        UUID userId = UUID.randomUUID();
        User user = localUser(userId, "encoded-current");
        ChangePasswordRequest request = changePasswordRequest("current-password", "new-password123", "different-password");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-password", "encoded-current")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(request));

        verify(refreshTokenService, never()).removeAllByUserId(userId);
    }

    @Test
    @DisplayName("새 비밀번호는 현재 비밀번호와 같을 수 없다")
    void changePasswordFailsWhenNewPasswordMatchesCurrentPassword() {
        UUID userId = UUID.randomUUID();
        User user = localUser(userId, "encoded-current");
        ChangePasswordRequest request = changePasswordRequest("same-password", "same-password", "same-password");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("same-password", "encoded-current")).thenReturn(true);

        assertThrows(InvalidPasswordException.class, () -> userService.changePassword(request));

        verify(refreshTokenService, never()).removeAllByUserId(userId);
    }

    private void authenticate(UUID userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null)
        );
    }

    private ChangePasswordRequest changePasswordRequest(String currentPassword, String newPassword, String newPasswordConfirm) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        ReflectionTestUtils.setField(request, "currentPassword", currentPassword);
        ReflectionTestUtils.setField(request, "newPassword", newPassword);
        ReflectionTestUtils.setField(request, "newPasswordConfirm", newPasswordConfirm);
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
