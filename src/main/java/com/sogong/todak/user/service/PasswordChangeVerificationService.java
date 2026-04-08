package com.sogong.todak.user.service;

import com.sogong.todak.common.exception.EmailVerificationCodeAlreadyUsedException;
import com.sogong.todak.common.exception.EmailVerificationCodeExpiredException;
import com.sogong.todak.common.exception.EmailVerificationCodeMismatchException;
import com.sogong.todak.common.exception.EmailVerificationCodeNotFoundException;
import com.sogong.todak.common.exception.TooManyRequestsException;
import com.sogong.todak.user.config.EmailVerificationProperties;
import com.sogong.todak.user.dto.response.PasswordChangeCodeSendResponse;
import com.sogong.todak.user.entity.PasswordChangeVerification;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.repository.PasswordChangeVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordChangeVerificationService {

    private final PasswordChangeVerificationRepository passwordChangeVerificationRepository;
    private final PasswordChangeVerificationMailService passwordChangeVerificationMailService;
    private final EmailVerificationProperties emailVerificationProperties;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordChangeCodeSendResponse sendCode(User user) {
        String email = requireEmail(user);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        PasswordChangeVerification verification = passwordChangeVerificationRepository.findByUserIdForUpdate(user.getUserId())
                .orElse(null);

        Duration resendInterval = Duration.ofSeconds(emailVerificationProperties.getCode().getResendIntervalSeconds());
        if (verification != null && !verification.canResend(now, resendInterval)) {
            long waitSeconds = Duration.between(now, verification.getSentAt().plus(resendInterval)).toSeconds();
            throw new TooManyRequestsException("인증코드 재전송은 %d초 후에 가능합니다.".formatted(Math.max(1, waitSeconds)));
        }

        String rawCode = generateNumericCode(emailVerificationProperties.getCode().getLength());
        String encodedCode = passwordEncoder.encode(rawCode);
        OffsetDateTime expiresAt = now.plusSeconds(emailVerificationProperties.getCode().getTtlSeconds());

        if (verification == null) {
            verification = PasswordChangeVerification.issue(user.getUserId(), email, encodedCode, now, expiresAt);
        } else {
            verification.reissue(email, encodedCode, now, expiresAt);
        }

        passwordChangeVerificationRepository.save(verification);
        passwordChangeVerificationMailService.sendPasswordChangeCode(email, rawCode, emailVerificationProperties.getCode().getTtlSeconds());

        return PasswordChangeCodeSendResponse.builder()
                .message("인증코드를 이메일로 발송했습니다.")
                .maskedEmail(maskEmail(email))
                .expiresInSeconds(emailVerificationProperties.getCode().getTtlSeconds())
                .resendAvailableInSeconds(emailVerificationProperties.getCode().getResendIntervalSeconds())
                .build();
    }

    public void verifyCode(User user, String verificationCode) {
        String email = requireEmail(user);
        PasswordChangeVerification verification = loadVerificationForUpdate(user.getUserId());
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (!Objects.equals(email, normalizeEmail(verification.getEmail()))) {
            throw new EmailVerificationCodeNotFoundException("현재 이메일에 발급된 인증코드가 없습니다. 다시 인증코드를 발급해주세요.");
        }

        if (verification.isUsed()) {
            throw new EmailVerificationCodeAlreadyUsedException("이미 사용된 인증코드입니다. 다시 인증코드를 발급해주세요.");
        }

        if (verification.isExpired(now)) {
            throw new EmailVerificationCodeExpiredException("인증코드가 만료되었습니다. 다시 인증코드를 발급해주세요.");
        }

        if (!passwordEncoder.matches(verificationCode, verification.getCodeHash())) {
            int failedAttempts = verification.increaseFailedAttempts();
            if (failedAttempts >= emailVerificationProperties.getCode().getMaxAttempts()) {
                verification.expireAt(now);
                throw new TooManyRequestsException("인증코드 입력 횟수를 초과했습니다. 다시 인증코드를 발급해주세요.");
            }
            throw new EmailVerificationCodeMismatchException("인증코드가 일치하지 않습니다.");
        }
    }

    public void consumeCode(UUID userId) {
        PasswordChangeVerification verification = loadVerificationForUpdate(userId);
        verification.markUsed(OffsetDateTime.now(ZoneOffset.UTC));
    }

    private PasswordChangeVerification loadVerificationForUpdate(UUID userId) {
        return passwordChangeVerificationRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new EmailVerificationCodeNotFoundException("인증코드를 먼저 발송해주세요."));
    }

    private String requireEmail(User user) {
        String email = normalizeEmail(user.getEmail());
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("이메일이 등록되지 않은 계정입니다.");
        }
        return email;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + email.substring(Math.max(atIndex, 0));
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (localPart.length() <= 3) {
            return localPart.charAt(0) + "**" + domain;
        }

        return localPart.substring(0, 3) + "***" + domain;
    }

    private String generateNumericCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(secureRandom.nextInt(10));
        }
        return builder.toString();
    }
}
