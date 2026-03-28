package com.sogong.todak.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sogong.todak.auth.dto.request.SignupRequest;
import com.sogong.todak.auth.dto.response.AuthResponse;
import com.sogong.todak.auth.jwt.JwtTokenProvider;
import com.sogong.todak.auth.oauth2.service.LocalAuthService;
import com.sogong.todak.auth.oauth2.service.OAuthExchangeService;
import com.sogong.todak.auth.refresh.service.RefreshTokenService;
import com.sogong.todak.auth.service.AuthWithdrawalService;
import com.sogong.todak.auth.service.EmailAccountStatusService;
import com.sogong.todak.common.exception.DuplicateResourceException;
import com.sogong.todak.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerValidationTest {

    private MockMvc mockMvc;
    private LocalAuthService localAuthService;
    private AuthWithdrawalService authWithdrawalService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        localAuthService = mock(LocalAuthService.class);
        OAuthExchangeService oAuthExchangeService = mock(OAuthExchangeService.class);
        authWithdrawalService = mock(AuthWithdrawalService.class);
        EmailAccountStatusService emailAccountStatusService = mock(EmailAccountStatusService.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);

        AuthController controller = new AuthController(
                localAuthService,
                oAuthExchangeService,
                authWithdrawalService,
                emailAccountStatusService,
                refreshTokenService,
                jwtTokenProvider
        );
        ReflectionTestUtils.setField(controller, "kakaoAuthorizationUri", "/oauth2/authorization/kakao");

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("회원가입 요청에 nickname이 없으면 400을 반환한다")
    void localSignupWithoutNicknameReturnsBadRequest() throws Exception {
        Map<String, Object> request = Map.of(
                "email", "test@example.com",
                "password", "password1234"
        );

        mockMvc.perform(post("/api/v1/auth/local/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("닉네임은 필수입니다."))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/local/signup"));
    }

    @Test
    @DisplayName("LOCAL signup은 비밀번호 없이 요청할 수 없다")
    void localSignupWithoutPasswordReturnsBadRequest() throws Exception {
        Map<String, Object> request = Map.of(
                "email", "test@example.com",
                "nickname", "tester"
        );

        mockMvc.perform(post("/api/v1/auth/local/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호는 필수입니다."))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/local/signup"));
    }

    @Test
    @DisplayName("회원가입 시 중복 이메일 또는 닉네임이면 409를 반환한다")
    void localSignupDuplicateReturnsConflict() throws Exception {
        when(localAuthService.signup(any()))
                .thenThrow(new DuplicateResourceException("이미 존재하는 이메일입니다."));

        Map<String, Object> request = Map.of(
                "email", "test@example.com",
                "password", "password1234",
                "nickname", "tester"
        );

        mockMvc.perform(post("/api/v1/auth/local/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/local/signup"));
    }

    @Test
    @DisplayName("회원가입 중 이메일 unique 제약 위반이 발생해도 409를 반환한다")
    void localSignupDataIntegrityViolationReturnsConflict() throws Exception {
        when(localAuthService.signup(any()))
                .thenThrow(new DataIntegrityViolationException(
                        "could not execute statement",
                        new RuntimeException("duplicate key value violates unique constraint \"ux_users_email\"")
                ));

        Map<String, Object> request = Map.of(
                "email", "test@example.com",
                "password", "password1234",
                "nickname", "tester"
        );

        mockMvc.perform(post("/api/v1/auth/local/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/local/signup"));
    }

    @Test
    @DisplayName("회원가입 요청 스펙에는 profileImageUrl 필드가 없다")
    void signupRequestDoesNotDeclareProfileImageUrl() {
        boolean hasProfileImageField = java.util.Arrays.stream(SignupRequest.class.getDeclaredFields())
                .anyMatch(field -> field.getName().equals("profileImageUrl"));

        org.junit.jupiter.api.Assertions.assertFalse(hasProfileImageField);
    }

    @Test
    @DisplayName("회원가입 요청은 프로필 이미지 없이도 생성된다")
    void localSignupWithoutProfileImageReturnsCreated() throws Exception {
        when(localAuthService.signup(any())).thenReturn(AuthResponse.builder()
                .isNewUser(true)
                .build());

        Map<String, Object> request = Map.of(
                "email", "test@example.com",
                "password", "password1234",
                "nickname", "tester"
        );

        mockMvc.perform(post("/api/v1/auth/local/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("복구 응답이면 회원가입 API는 200을 반환한다")
    void localSignupRestoreReturnsOk() throws Exception {
        when(localAuthService.signup(any())).thenReturn(AuthResponse.builder()
                .isNewUser(false)
                .build());

        Map<String, Object> request = Map.of(
                "email", "deleted@example.com",
                "password", "password1234",
                "nickname", "tester"
        );

        mockMvc.perform(post("/api/v1/auth/local/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원 탈퇴 요청은 204를 반환한다")
    void withdrawCurrentUserReturnsNoContent() throws Exception {
        Map<String, Object> request = Map.of("password", "password1234");

        mockMvc.perform(delete("/api/v1/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authWithdrawalService).withdrawCurrentUser(any());
    }
}
