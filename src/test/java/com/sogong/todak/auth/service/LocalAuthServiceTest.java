package com.sogong.todak.auth.service;

import com.sogong.todak.auth.dto.request.LoginRequest;
import com.sogong.todak.auth.jwt.JwtTokenProvider;
import com.sogong.todak.auth.oauth2.service.LocalAuthService;
import com.sogong.todak.auth.refresh.service.RefreshTokenService;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
