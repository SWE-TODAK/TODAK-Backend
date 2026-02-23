package com.sogong.todak.auth.oauth2.service;

import com.sogong.todak.auth.domain.AuthProvider;
import com.sogong.todak.auth.oauth2.userinfo.KakaoUserInfo;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.entity.UserIdentity;
import com.sogong.todak.user.repository.UserIdentityRepository;
import com.sogong.todak.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 소셜 서비스로부터 사용자 정보 로드
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = mapProvider(registrationId);

        // 2. 카카오 유저 정보 추출
        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        String providerUserId = kakaoUserInfo.getProviderId();

        if (providerUserId == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_provider_id"));
        }

        // 3. 기존 연동 여부 확인 (UserIdentity 기반 조회)
        User user = userIdentityRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(UserIdentity::getUser)
                .orElseGet(() -> registerNewSocialUser(provider, kakaoUserInfo));

        // 4. 최신 프로필 정보 동기화
        user.syncOAuth2Profile(
                kakaoUserInfo.getEmail(),
                ensureUniqueNickname(kakaoUserInfo.getNickname()),
                kakaoUserInfo.getProfileImageUrl(),
                kakaoUserInfo.getBirthDate() // 추가: 생년월일 전달
        );

        // 5. 성공 핸들러를 위한 추가 속성 구성
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                makeAttributes(oAuth2User.getAttributes(), user, providerUserId),
                "app_user_id"
        );
    }

    /**
     * 신규 소셜 사용자 등록
     * UserAuth(자체 비밀번호) 없이 User와 UserIdentity만 생성함
     */
    private User registerNewSocialUser(AuthProvider provider, KakaoUserInfo userInfo) {
        String uniqueNickname = ensureUniqueNickname(userInfo.getNickname());

        // 프로필 생성
        User newUser = User.builder()
                .email(userInfo.getEmail())
                .nickname(uniqueNickname)
                .profileImageUrl(userInfo.getProfileImageUrl())
                .birthDate(userInfo.getBirthDate())
                .build();

        userRepository.save(newUser);

        // 소셜 연동 정보 생성
        UserIdentity identity = UserIdentity.builder()
                .user(newUser)
                .provider(provider)
                .providerUserId(userInfo.getProviderId())
                .providerEmail(userInfo.getEmail())
                .build();

        userIdentityRepository.save(identity);

        log.info("New Social User Registered: userId={}, provider={}", newUser.getUserId(), provider);
        return newUser;
    }

    private String ensureUniqueNickname(String nickname) {
        String base = (nickname == null || nickname.isBlank()) ? "토닥이" : nickname;
        if (!userRepository.existsByNickname(base)) return base;

        return base + "_" + UUID.randomUUID().toString().substring(0, 5);
    }

    private Map<String, Object> makeAttributes(Map<String, Object> original, User user, String pId) {
        Map<String, Object> attributes = new HashMap<>(original);
        attributes.put("app_user_id", user.getUserId()); // UUID 객체 유지
        attributes.put("provider_user_id", pId);
        return attributes;
    }

    private AuthProvider mapProvider(String id) {
        if ("kakao".equalsIgnoreCase(id)) return AuthProvider.KAKAO;
        throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"));
    }
}