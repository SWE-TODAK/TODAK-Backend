package com.sogong.todak.auth.service;

import com.sogong.todak.auth.dto.request.SignupRequest;
import com.sogong.todak.auth.dto.request.LoginRequest;
import com.sogong.todak.auth.jwt.JwtTokenProvider;
import com.sogong.todak.auth.oauth2.service.LocalAuthService;
import com.sogong.todak.auth.refresh.service.RefreshTokenService;
import com.sogong.todak.auth.dto.response.AuthResponse;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.entity.UserAuth;
import com.sogong.todak.user.entity.UserIdentity;
import com.sogong.todak.user.repository.UserAuthRepository;
import com.sogong.todak.user.repository.UserIdentityRepository;
import com.sogong.todak.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAuthRepository userAuthRepository;

    @Mock
    private UserIdentityRepository userIdentityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("LOCAL 회원가입 birthDate 저장 로직은 유지된다")
    void signupKeepsBirthDateForLocalUser() {
        LocalAuthService localAuthService = new LocalAuthService(
                userRepository,
                userAuthRepository,
                userIdentityRepository,
                passwordEncoder,
                jwtTokenProvider,
                refreshTokenService
        );

        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", "local@example.com");
        ReflectionTestUtils.setField(request, "password", "password1234");
        ReflectionTestUtils.setField(request, "nickname", "로컬유저");
        ReflectionTestUtils.setField(request, "birthDate", LocalDate.of(1998, 7, 14));

        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNull("local@example.com")).thenReturn(Optional.empty());
        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNotNull("local@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userAuthRepository.findByUserId(any())).thenReturn(Optional.empty());
        when(userAuthRepository.save(any(UserAuth.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userIdentityRepository.findAllByUser_UserId(any())).thenReturn(List.of());
        when(jwtTokenProvider.createAccessToken(any(), any())).thenReturn("access-token");
        when(jwtTokenProvider.getAccessExpiresInSeconds()).thenReturn(1800L);
        when(refreshTokenService.issue(any())).thenReturn("refresh-token");

        AuthResponse response = localAuthService.signup(request);

        assertEquals(LocalDate.of(1998, 7, 14), response.getUser().getBirthDate());
        assertEquals(true, response.isNewUser());
    }

    @Test
    @DisplayName("탈퇴한 LOCAL 유저는 기존 비밀번호와 달라도 새 비밀번호로 복구된다")
    void signupRestoresDeletedLocalUserWithNewPassword() {
        LocalAuthService localAuthService = new LocalAuthService(
                userRepository,
                userAuthRepository,
                userIdentityRepository,
                passwordEncoder,
                jwtTokenProvider,
                refreshTokenService
        );

        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", "deleted@example.com");
        ReflectionTestUtils.setField(request, "password", "new-password123");
        ReflectionTestUtils.setField(request, "nickname", "복구유저");
        ReflectionTestUtils.setField(request, "birthDate", LocalDate.of(2000, 1, 2));

        User user = User.builder()
                .email("deleted@example.com")
                .nickname("old-nickname")
                .build();
        ReflectionTestUtils.setField(user, "userId", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "deletedAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(user, "identities", new ArrayList<>());

        UserAuth userAuth = UserAuth.builder()
                .user(user)
                .passwordHash("old-encoded-password")
                .build();
        ReflectionTestUtils.setField(user, "auth", userAuth);

        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNull("deleted@example.com")).thenReturn(Optional.empty());
        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNotNull("deleted@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password123")).thenReturn("new-encoded-password");
        when(userAuthRepository.findByUserId(user.getUserId())).thenReturn(Optional.of(userAuth));
        when(userIdentityRepository.findAllByUser_UserId(user.getUserId())).thenReturn(List.of());
        when(jwtTokenProvider.createAccessToken(eq(user.getUserId()), any())).thenReturn("access-token");
        when(jwtTokenProvider.getAccessExpiresInSeconds()).thenReturn(1800L);
        when(refreshTokenService.issue(user.getUserId())).thenReturn("refresh-token");

        AuthResponse response = localAuthService.signup(request);

        assertFalse(user.isDeleted());
        assertFalse(response.isNewUser());
        assertEquals("복구유저", user.getNickname());
        assertEquals("new-encoded-password", userAuth.getPasswordHash());
    }

    @Test
    @DisplayName("탈퇴한 LOCAL 유저를 회원가입으로 복구한 뒤 새 비밀번호로 로그인할 수 있다")
    void loginWithNewPasswordAfterRestoreSignup() {
        LocalAuthService localAuthService = new LocalAuthService(
                userRepository,
                userAuthRepository,
                userIdentityRepository,
                passwordEncoder,
                jwtTokenProvider,
                refreshTokenService
        );

        SignupRequest signupRequest = new SignupRequest();
        ReflectionTestUtils.setField(signupRequest, "email", "deleted@example.com");
        ReflectionTestUtils.setField(signupRequest, "password", "new-password123");
        ReflectionTestUtils.setField(signupRequest, "nickname", "복구유저");

        LoginRequest loginRequest = new LoginRequest();
        ReflectionTestUtils.setField(loginRequest, "email", "deleted@example.com");
        ReflectionTestUtils.setField(loginRequest, "password", "new-password123");

        User user = User.builder()
                .email("deleted@example.com")
                .nickname("old-nickname")
                .build();
        ReflectionTestUtils.setField(user, "userId", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "deletedAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(user, "identities", new ArrayList<>());

        UserAuth userAuth = UserAuth.builder()
                .user(user)
                .passwordHash("old-encoded-password")
                .build();
        ReflectionTestUtils.setField(user, "auth", userAuth);

        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNull("deleted@example.com")).thenReturn(Optional.empty());
        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNotNull("deleted@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password123")).thenReturn("new-encoded-password");
        when(userAuthRepository.findByUserId(user.getUserId())).thenReturn(Optional.of(userAuth));
        when(userIdentityRepository.findAllByUser_UserId(user.getUserId())).thenReturn(List.of());
        when(userAuthRepository.findByUser_EmailAndUser_DeletedAtIsNull("deleted@example.com")).thenReturn(Optional.of(userAuth));
        when(passwordEncoder.matches("new-password123", "new-encoded-password")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(eq(user.getUserId()), any())).thenReturn("access-token");
        when(jwtTokenProvider.getAccessExpiresInSeconds()).thenReturn(1800L);
        when(refreshTokenService.issue(user.getUserId())).thenReturn("refresh-token");

        localAuthService.signup(signupRequest);
        AuthResponse loginResponse = localAuthService.login(loginRequest);

        assertFalse(loginResponse.isNewUser());
        assertEquals("new-encoded-password", user.getAuth().getPasswordHash());
    }

    @Test
    @DisplayName("deleted LOCAL 유저는 login으로 자동 복구되지 않는다")
    void loginDoesNotRestoreDeletedLocalUser() {
        LocalAuthService localAuthService = new LocalAuthService(
                userRepository,
                userAuthRepository,
                userIdentityRepository,
                passwordEncoder,
                jwtTokenProvider,
                refreshTokenService
        );

        LoginRequest loginRequest = new LoginRequest();
        ReflectionTestUtils.setField(loginRequest, "email", "deleted@example.com");
        ReflectionTestUtils.setField(loginRequest, "password", "password1234");

        when(userAuthRepository.findByUser_EmailAndUser_DeletedAtIsNull("deleted@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> localAuthService.login(loginRequest));
    }

    @Test
    @DisplayName("탈퇴한 KAKAO 유저가 LOCAL 회원가입하면 기존 row를 재사용하고 UserAuth를 생성한다")
    void signupReusesDeletedKakaoUserForLocalAccount() {
        LocalAuthService localAuthService = new LocalAuthService(
                userRepository,
                userAuthRepository,
                userIdentityRepository,
                passwordEncoder,
                jwtTokenProvider,
                refreshTokenService
        );

        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", "kakao@example.com");
        ReflectionTestUtils.setField(request, "password", "password1234");
        ReflectionTestUtils.setField(request, "nickname", "전환유저");
        ReflectionTestUtils.setField(request, "birthDate", LocalDate.of(1999, 9, 9));

        User deletedUser = User.builder()
                .email("kakao@example.com")
                .nickname("old-kakao")
                .profileImageUrl("https://example.com/old-kakao.png")
                .build();
        ReflectionTestUtils.setField(deletedUser, "userId", UUID.randomUUID());
        ReflectionTestUtils.setField(deletedUser, "deletedAt", OffsetDateTime.now());

        UserIdentity kakaoIdentity = UserIdentity.builder()
                .user(deletedUser)
                .provider(com.sogong.todak.auth.domain.AuthProvider.KAKAO)
                .providerUserId("12345")
                .providerEmail("kakao@example.com")
                .build();
        ReflectionTestUtils.setField(deletedUser, "identities", new ArrayList<>(List.of(kakaoIdentity)));

        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNull("kakao@example.com")).thenReturn(Optional.empty());
        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNotNull("kakao@example.com")).thenReturn(Optional.of(deletedUser));
        when(userAuthRepository.findByUserId(deletedUser.getUserId())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password1234")).thenReturn("encoded-password");
        when(userAuthRepository.save(any(UserAuth.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userIdentityRepository.findByUser_UserIdAndProvider(deletedUser.getUserId(), com.sogong.todak.auth.domain.AuthProvider.KAKAO))
                .thenReturn(Optional.of(kakaoIdentity));
        when(userIdentityRepository.findAllByUser_UserId(deletedUser.getUserId())).thenReturn(List.of());
        when(jwtTokenProvider.createAccessToken(eq(deletedUser.getUserId()), any())).thenReturn("access-token");
        when(jwtTokenProvider.getAccessExpiresInSeconds()).thenReturn(1800L);
        when(refreshTokenService.issue(deletedUser.getUserId())).thenReturn("refresh-token");

        AuthResponse response = localAuthService.signup(request);

        assertFalse(deletedUser.isDeleted());
        assertEquals("전환유저", deletedUser.getNickname());
        assertNull(deletedUser.getProfileImageUrl());
        assertFalse(response.isNewUser());
        verify(userRepository, never()).save(any(User.class));
        verify(userAuthRepository).save(any(UserAuth.class));
        verify(userIdentityRepository).delete(kakaoIdentity);
    }
}
