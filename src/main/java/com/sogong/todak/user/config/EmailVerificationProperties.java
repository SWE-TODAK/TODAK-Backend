package com.sogong.todak.user.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.email-verification")
public class EmailVerificationProperties {

    @Valid
    private final Code code = new Code();

    @Valid
    private final Mail mail = new Mail();

    @Getter
    @Setter
    public static class Code {

        @Min(60)
        @Max(1800)
        private long ttlSeconds = 300;

        @Min(4)
        @Max(8)
        private int length = 6;

        @Min(0)
        @Max(600)
        private long resendIntervalSeconds = 60;

        @Min(1)
        @Max(10)
        private int maxAttempts = 5;
    }

    @Getter
    @Setter
    public static class Mail {

        private boolean enabled = false;

        @NotBlank
        private String from = "no-reply@todak.local";
    }
}
