package com.sogong.todak.auth.service;

import com.sogong.todak.auth.dto.request.SignupRequest;
import com.sogong.todak.auth.dto.request.LoginRequest;
import com.sogong.todak.auth.jwt.JwtTokenProvider;
import com.sogong.todak.auth.oauth2.service.LocalAuthService;
import com.sogong.todak.auth.refresh.service.RefreshTokenService;
import com.sogong.todak.auth.dto.response.AuthResponse;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.entity.UserAuth;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

        when(userRepository.existsByEmailAndDeletedAtIsNull("local@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userAuthRepository.save(any(UserAuth.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userIdentityRepository.findAllByUser_UserId(any())).thenReturn(List.of());
        when(jwtTokenProvider.createAccessToken(any(), any())).thenReturn("access-token");
        when(jwtTokenProvider.getAccessExpiresInSeconds()).thenReturn(1800L);
        when(refreshTokenService.issue(any())).thenReturn("refresh-token");

        AuthResponse response = localAuthService.signup(request);

        assertEquals(LocalDate.of(1998, 7, 14), response.getUser().getBirthDate());
    }

    @Test
    @DisplayName("탈퇴한 유저는 로컬 로그인할 수 없다")
    void loginFailsWhenUserIsSoftDeleted() {
        LocalAuthService localAuthService = new LocalAuthService(
                userRepository,
                userAuthRepository,
                userIdentityRepository,
                passwordEncoder,
                jwtTokenProvider,
                refreshTokenService
        );

        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", "deleted@example.com");
        ReflectionTestUtils.setField(request, "password", "password1234");

        when(userAuthRepository.findByUser_EmailAndUser_DeletedAtIsNull("deleted@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> localAuthService.login(request));
    }
}
