package com.sogong.todak.auth.config;

import com.sogong.todak.auth.oauth2.cookie.HttpCookieOAuth2AuthorizationRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Configuration
public class OAuth2CookieRepositoryConfig {

    // 프로덕션(HTTPS) 환경에서는 true, 로컬(HTTP) 환경에서는 false로 설정
    // application.yml에 app.security.cookie.secure 설정이 없으면 기본값 false 사용
    @Value("${app.security.cookie.secure:false}")
    private boolean cookieSecure;

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        // 생성 시 secure 설정을 주입하여 환경에 유연하게 대응합니다.
        return new HttpCookieOAuth2AuthorizationRequestRepository(cookieSecure);
    }
}