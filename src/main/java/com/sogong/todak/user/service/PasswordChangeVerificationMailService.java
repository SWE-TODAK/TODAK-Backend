package com.sogong.todak.user.service;

import com.sogong.todak.common.exception.ExternalApiException;
import com.sogong.todak.user.config.EmailVerificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordChangeVerificationMailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationProperties emailVerificationProperties;

    public void sendPasswordChangeCode(String to, String verificationCode, long ttlSeconds) {
        if (!emailVerificationProperties.getMail().isEnabled()) {
            log.info("[PasswordChangeCode] mail disabled. to={}, code={}, ttlSeconds={}", to, verificationCode, ttlSeconds);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailVerificationProperties.getMail().getFrom());
        message.setTo(to);
        message.setSubject("[Todak] 비밀번호 변경 인증코드");
        message.setText(buildBody(verificationCode, ttlSeconds));

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new ExternalApiException("인증 메일 발송에 실패했습니다.", ex);
        }
    }

    private String buildBody(String verificationCode, long ttlSeconds) {
        long ttlMinutes = Math.max(1, ttlSeconds / 60);

        return """
                안녕하세요, Todak입니다.

                비밀번호 변경 인증코드는 아래와 같습니다.

                인증코드: %s

                본 코드는 약 %d분 동안 유효하며 1회만 사용할 수 있습니다.
                본인이 요청하지 않았다면 이 메일을 무시해주세요.
                """.formatted(verificationCode, ttlMinutes);
    }
}
