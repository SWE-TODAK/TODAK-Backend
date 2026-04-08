package com.sogong.todak.user.service;

import com.sogong.todak.common.exception.EmailVerificationCodeExpiredException;
import com.sogong.todak.common.exception.EmailVerificationCodeMismatchException;
import com.sogong.todak.common.exception.EmailVerificationCodeNotFoundException;
import com.sogong.todak.common.exception.TooManyRequestsException;
import com.sogong.todak.user.config.EmailVerificationProperties;
import com.sogong.todak.user.dto.response.PasswordChangeCodeSendResponse;
import com.sogong.todak.user.entity.PasswordChangeVerification;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.repository.PasswordChangeVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordChangeVerificationServiceTest {

    @Mock
    private PasswordChangeVerificationRepository passwordChangeVerificationRepository;

    @Mock
    private PasswordChangeVerificationMailService passwordChangeVerificationMailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordChangeVerificationService passwordChangeVerificationService;

    @BeforeEach
    void setUp() {
        EmailVerificationProperties emailVerificationProperties = new EmailVerificationProperties();
        emailVerificationProperties.getCode().setTtlSeconds(300);
        emailVerificationProperties.getCode().setLength(6);
        emailVerificationProperties.getCode().setResendIntervalSeconds(60);
        emailVerificationProperties.getCode().setMaxAttempts(3);

        passwordChangeVerificationService = new PasswordChangeVerificationService(
                passwordChangeVerificationRepository,
                passwordChangeVerificationMailService,
                emailVerificationProperties,
                passwordEncoder
        );
    }

    @Test
    @DisplayName("인증코드 발송은 새 코드를 저장하고 메일 발송을 호출한다")
    void sendCodeSuccess() {
        UUID userId = UUID.randomUUID();
        User user = user(userId, "local@example.com");

        when(passwordChangeVerificationRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded-code");

        PasswordChangeCodeSendResponse response = passwordChangeVerificationService.sendCode(user);

        assertEquals("인증코드를 이메일로 발송했습니다.", response.getMessage());
        assertEquals("loc***@example.com", response.getMaskedEmail());

        ArgumentCaptor<PasswordChangeVerification> captor = ArgumentCaptor.forClass(PasswordChangeVerification.class);
        verify(passwordChangeVerificationRepository).save(captor.capture());
        verify(passwordChangeVerificationMailService).sendPasswordChangeCode(eq("local@example.com"), any(), eq(300L));
        assertEquals(userId, captor.getValue().getUserId());
    }

    @Test
    @DisplayName("재전송 제한 시간 내 재발송은 429 예외를 던진다")
    void sendCodeFailsWhenResendRequestedTooSoon() {
        UUID userId = UUID.randomUUID();
        User user = user(userId, "local@example.com");
        PasswordChangeVerification verification = PasswordChangeVerification.issue(
                userId,
                "local@example.com",
                "encoded-code",
                OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5)
        );

        when(passwordChangeVerificationRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(verification));

        assertThrows(TooManyRequestsException.class, () -> passwordChangeVerificationService.sendCode(user));
    }

    @Test
    @DisplayName("미발송 상태에서 인증코드 검증 시 예외를 던진다")
    void verifyCodeFailsWhenCodeNotIssued() {
        UUID userId = UUID.randomUUID();
        User user = user(userId, "local@example.com");

        when(passwordChangeVerificationRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.empty());

        assertThrows(EmailVerificationCodeNotFoundException.class,
                () -> passwordChangeVerificationService.verifyCode(user, "123456"));
    }

    @Test
    @DisplayName("인증코드가 만료되면 예외를 던진다")
    void verifyCodeFailsWhenExpired() {
        UUID userId = UUID.randomUUID();
        User user = user(userId, "local@example.com");
        PasswordChangeVerification verification = PasswordChangeVerification.issue(
                userId,
                "local@example.com",
                "encoded-code",
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(6),
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1)
        );

        when(passwordChangeVerificationRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(verification));

        assertThrows(EmailVerificationCodeExpiredException.class,
                () -> passwordChangeVerificationService.verifyCode(user, "123456"));
    }

    @Test
    @DisplayName("인증코드가 일치하지 않으면 실패 횟수를 증가시키고 예외를 던진다")
    void verifyCodeFailsWhenMismatch() {
        UUID userId = UUID.randomUUID();
        User user = user(userId, "local@example.com");
        PasswordChangeVerification verification = PasswordChangeVerification.issue(
                userId,
                "local@example.com",
                "encoded-code",
                OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(30),
                OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5)
        );

        when(passwordChangeVerificationRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(verification));
        when(passwordEncoder.matches("123456", "encoded-code")).thenReturn(false);

        assertThrows(EmailVerificationCodeMismatchException.class,
                () -> passwordChangeVerificationService.verifyCode(user, "123456"));
        assertEquals(1, verification.getFailedAttempts());
    }

    private User user(UUID userId, String email) {
        User user = User.builder()
                .email(email)
                .nickname("tester")
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);
        return user;
    }
}
