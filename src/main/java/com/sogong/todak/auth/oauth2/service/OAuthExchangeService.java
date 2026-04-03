package com.sogong.todak.auth.oauth2.service;

import com.sogong.todak.auth.dto.request.OAuthExchangeRequest;
import com.sogong.todak.auth.dto.response.AuthResult;
import com.sogong.todak.auth.dto.response.AuthResponse;
import com.sogong.todak.auth.dto.response.TokenPairResponse;
import com.sogong.todak.auth.dto.response.UserSummaryResponse;
import com.sogong.todak.auth.jwt.JwtTokenProvider;
import com.sogong.todak.auth.oauth2.exchange.ExchangeCodePayload;
import com.sogong.todak.auth.oauth2.exchange.ExchangeCodeStore;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.repository.UserIdentityRepository;
import com.sogong.todak.user.repository.UserRepository;
import com.sogong.todak.auth.refresh.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthExchangeService {

    private final ExchangeCodeStore exchangeCodeStore;
    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.access-ttl-seconds:1800}")
    private long accessTtlSeconds;

    @Transactional
    public AuthResponse exchange(OAuthExchangeRequest request) {

        ExchangeCodePayload payload = exchangeCodeStore.consume(request.getCode())
                .orElseThrow(() -> new AuthExchangeException(AuthExchangeError.INVALID_OR_EXPIRED_CODE));

        UUID userId = payload.userId();
        AuthResult authResult = payload.authResult();

        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AuthExchangeException(AuthExchangeError.USER_NOT_FOUND));

        List<String> providers = userIdentityRepository.findAllByUser(user)
                .stream()
                .map(identity -> identity.getProvider().name())
                .distinct()
                .toList();

        String accessToken = jwtTokenProvider.createAccessToken(userId, "ROLE_USER");

        String refreshToken = refreshTokenService.issue(userId);

        TokenPairResponse tokenPair = TokenPairResponse.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresInSeconds(accessTtlSeconds) // 또는 jwtTokenProvider.getAccessExpiresInSeconds()
                .rotated(true)
                .build();

        UserSummaryResponse userSummary = UserSummaryResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .profileImageUrl(user.getProfileImageUrl())
                .providers(providers)
                .build();

        return AuthResponse.builder()
                .authResult(authResult)
                .token(tokenPair)
                .user(userSummary)
                .build();
    }
}
