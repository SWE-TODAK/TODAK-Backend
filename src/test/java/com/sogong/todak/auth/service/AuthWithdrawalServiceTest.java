package com.sogong.todak.auth.service;

import com.sogong.todak.auth.domain.AuthProvider;
import com.sogong.todak.auth.dto.request.WithdrawRequest;
import com.sogong.todak.auth.refresh.service.RefreshTokenService;
import com.sogong.todak.common.exception.AlreadyWithdrawnUserException;
import com.sogong.todak.common.exception.InvalidPasswordException;
import com.sogong.todak.common.exception.KakaoUnlinkFailedException;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.entity.UserAuth;
import com.sogong.todak.user.entity.UserIdentity;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthWithdrawalServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private KakaoUnlinkService kakaoUnlinkService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthWithdrawalService authWithdrawalService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("LOCAL 유저는 비밀번호가 일치하면 탈퇴 성공")
    void withdrawLocalUserSuccess() {
        UUID userId = UUID.randomUUID();
        User user = localUser(userId, "encoded-password");
        WithdrawRequest request = withdrawRequest("plain-password");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);

        authWithdrawalService.withdrawCurrentUser(request);

        assertTrue(user.isDeleted());
        verify(refreshTokenService).removeAllByUserId(userId);
        verify(kakaoUnlinkService, never()).unlink(anyString());
    }

    @Test
    @DisplayName("LOCAL 유저는 비밀번호가 일치하지 않으면 탈퇴 실패")
    void withdrawLocalUserFailsWhenPasswordMismatch() {
        UUID userId = UUID.randomUUID();
        User user = localUser(userId, "encoded-password");
        WithdrawRequest request = withdrawRequest("wrong-password");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> authWithdrawalService.withdrawCurrentUser(request));

        assertTrue(!user.isDeleted());
        verify(refreshTokenService, never()).removeAllByUserId(userId);
    }

    @Test
    @DisplayName("KAKAO 유저는 unlink 성공 후 탈퇴 성공")
    void withdrawKakaoUserSuccess() {
        UUID userId = UUID.randomUUID();
        User user = kakaoUser(userId, "123456789");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        authWithdrawalService.withdrawCurrentUser(new WithdrawRequest());

        assertTrue(user.isDeleted());
        verify(kakaoUnlinkService).unlink("123456789");
        verify(refreshTokenService).removeAllByUserId(userId);
    }

    @Test
    @DisplayName("KAKAO unlink 실패 시 회원탈퇴 전체 실패")
    void withdrawKakaoUserFailsWhenUnlinkFails() {
        UUID userId = UUID.randomUUID();
        User user = kakaoUser(userId, "123456789");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        doThrow(new KakaoUnlinkFailedException("카카오 연결 해제에 실패했습니다."))
                .when(kakaoUnlinkService)
                .unlink("123456789");

        assertThrows(KakaoUnlinkFailedException.class,
                () -> authWithdrawalService.withdrawCurrentUser(new WithdrawRequest()));

        assertTrue(!user.isDeleted());
        verify(refreshTokenService, never()).removeAllByUserId(userId);
    }

    @Test
    @DisplayName("이미 탈퇴한 유저가 재요청하면 실패")
    void withdrawAlreadyDeletedUserFails() {
        UUID userId = UUID.randomUUID();
        User deletedUser = localUser(userId, "encoded-password");
        deletedUser.softDelete();

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());
        when(userRepository.findWithAuthAndIdentitiesByUserId(userId)).thenReturn(Optional.of(deletedUser));

        assertThrows(AlreadyWithdrawnUserException.class,
                () -> authWithdrawalService.withdrawCurrentUser(withdrawRequest("plain-password")));
    }

    @Test
    @DisplayName("탈퇴 성공 시 refresh token 제거를 호출한다")
    void withdrawRemovesRefreshTokens() {
        UUID userId = UUID.randomUUID();
        User user = localUser(userId, "encoded-password");

        authenticate(userId);
        when(userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);

        authWithdrawalService.withdrawCurrentUser(withdrawRequest("plain-password"));

        verify(refreshTokenService).removeAllByUserId(userId);
    }

    private void authenticate(UUID userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null)
        );
    }

    private WithdrawRequest withdrawRequest(String password) {
        WithdrawRequest request = new WithdrawRequest();
        ReflectionTestUtils.setField(request, "password", password);
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

    private User kakaoUser(UUID userId, String providerUserId) {
        User user = User.builder()
                .email("kakao@example.com")
                .nickname("kakao-user")
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);

        UserIdentity identity = UserIdentity.builder()
                .user(user)
                .provider(AuthProvider.KAKAO)
                .providerUserId(providerUserId)
                .providerEmail("kakao@example.com")
                .build();

        ArrayList<UserIdentity> identities = new ArrayList<>();
        identities.add(identity);
        ReflectionTestUtils.setField(user, "identities", identities);

        assertNotNull(user.getIdentities());
        return user;
    }
}
