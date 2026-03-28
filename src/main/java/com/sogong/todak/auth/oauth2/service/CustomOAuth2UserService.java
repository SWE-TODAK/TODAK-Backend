package com.sogong.todak.auth.oauth2.service;

import com.sogong.todak.auth.domain.AuthProvider;
import com.sogong.todak.auth.oauth2.userinfo.KakaoUserInfo;
import com.sogong.todak.auth.oauth2.userinfo.OAuthUserProfile;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.entity.UserIdentity;
import com.sogong.todak.user.repository.UserAuthRepository;
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
    private final UserAuthRepository userAuthRepository;
    private final UserIdentityRepository userIdentityRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 소셜 서비스로부터 사용자 정보 로드
        OAuth2User oAuth2User = loadProviderUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = mapProvider(registrationId);

        // 2. 카카오 유저 정보 추출
        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        OAuthUserProfile userProfile = kakaoUserInfo.toUserProfile();
        String providerUserId = kakaoUserInfo.getProviderId();

        if (providerUserId == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_provider_id"));
        }

        // 3. 기존 연동 여부 확인 (UserIdentity 기반 조회)
        ResolvedOAuthUser resolved = resolveUser(provider, providerUserId, userProfile);
        User user = resolved.user();

        user.replaceWithOAuth2Profile(normalizeProfile(userProfile));

        // 5. 성공 핸들러를 위한 추가 속성 구성
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                makeAttributes(oAuth2User.getAttributes(), user, providerUserId, resolved.isNewUser()),
                "app_user_id"
        );
    }

    private ResolvedOAuthUser resolveUser(AuthProvider provider, String providerUserId, OAuthUserProfile userProfile) {
        OAuthUserProfile normalizedProfile = normalizeProfile(userProfile);

        Optional<UserIdentity> linkedIdentity = userIdentityRepository.findByProviderAndProviderUserId(provider, providerUserId);
        if (linkedIdentity.isPresent()) {
            User user = linkedIdentity.get().getUser();
            if (user.isDeleted()) {
                if (user.hasAuth()) {
                    return new ResolvedOAuthUser(
                            resetDeletedUserForKakaoSignup(user, linkedIdentity.get(), provider, normalizedProfile, providerUserId),
                            false
                    );
                }
                return new ResolvedOAuthUser(
                        restoreDeletedSocialUser(user, linkedIdentity.get(), provider, normalizedProfile, providerUserId, normalizedProfile.getEmail()),
                        false
                );
            }
            return new ResolvedOAuthUser(user, false);
        }

        String email = normalizedProfile.getEmail();
        if (email != null) {
            Optional<User> activeUser = userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNull(email);
            if (activeUser.isPresent()) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("active_email_exists"),
                        "이미 활성화된 계정입니다."
                );
            }

            Optional<User> deletedUser = userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNotNull(email);
            if (deletedUser.isPresent()) {
                if (deletedUser.get().hasAuth()) {
                    return new ResolvedOAuthUser(
                            resetDeletedUserForKakaoSignup(deletedUser.get(), null, provider, normalizedProfile, providerUserId),
                            false
                    );
                }
                return new ResolvedOAuthUser(
                        restoreDeletedSocialUser(deletedUser.get(), null, provider, normalizedProfile, providerUserId, email),
                        false
                );
            }
        }

        return new ResolvedOAuthUser(
                registerNewSocialUser(provider, providerUserId, normalizedProfile),
                true
        );
    }

    private User registerNewSocialUser(AuthProvider provider, String providerUserId, OAuthUserProfile userProfile) {
        if (userProfile.getBirthDate() == null) {
            log.warn("Kakao birthDate is missing or invalid for providerUserId={}", providerUserId);
        }

        User newUser = User.builder()
                .email(userProfile.getEmail())
                .nickname(userProfile.getNickname())
                .profileImageUrl(userProfile.getProfileImageUrl())
                .birthDate(userProfile.getBirthDate())
                .build();

        userRepository.save(newUser);
        userIdentityRepository.save(UserIdentity.builder()
                .user(newUser)
                .provider(provider)
                .providerUserId(providerUserId)
                .providerEmail(userProfile.getEmail())
                .build());

        log.info("New Social User Registered: userId={}, provider={}", newUser.getUserId(), provider);
        return newUser;
    }

    private User restoreDeletedSocialUser(
            User deletedUser,
            UserIdentity identity,
            AuthProvider provider,
            OAuthUserProfile userProfile,
            String providerUserId,
            String providerEmail
    ) {
        deletedUser.restore();
        deletedUser.replaceWithOAuth2Profile(userProfile);

        UserIdentity targetIdentity = identity != null
                ? identity
                : userIdentityRepository.findByUser_UserIdAndProvider(deletedUser.getUserId(), provider).orElse(null);

        if (targetIdentity != null) {
            targetIdentity.relink(deletedUser, providerUserId, providerEmail);
        } else {
            userIdentityRepository.save(UserIdentity.builder()
                    .user(deletedUser)
                    .provider(provider)
                    .providerUserId(providerUserId)
                    .providerEmail(providerEmail)
                    .build());
        }
        return deletedUser;
    }

    private User resetDeletedUserForKakaoSignup(
            User deletedUser,
            UserIdentity identity,
            AuthProvider provider,
            OAuthUserProfile userProfile,
            String providerUserId
    ) {
        removeLocalAuth(deletedUser);
        deletedUser.restore();
        deletedUser.replaceWithOAuth2Profile(userProfile);

        UserIdentity targetIdentity = identity != null
                ? identity
                : userIdentityRepository.findByUser_UserIdAndProvider(deletedUser.getUserId(), provider).orElse(null);

        if (targetIdentity != null) {
            targetIdentity.relink(deletedUser, providerUserId, userProfile.getEmail());
        } else {
            userIdentityRepository.save(UserIdentity.builder()
                    .user(deletedUser)
                    .provider(provider)
                    .providerUserId(providerUserId)
                    .providerEmail(userProfile.getEmail())
                    .build());
        }
        return deletedUser;
    }

    private void removeLocalAuth(User user) {
        userAuthRepository.findByUserId(user.getUserId())
                .ifPresent(auth -> {
                    userAuthRepository.delete(auth);
                    user.clearAuth();
                });
    }

    private String ensureNickname(String nickname) {
        return normalizeNickname(nickname);
    }

    private String normalizeNickname(String nickname) {
        return (nickname == null || nickname.isBlank()) ? "토닥이" : nickname;
    }

    private Map<String, Object> makeAttributes(Map<String, Object> original, User user, String pId, boolean isNewUser) {
        Map<String, Object> attributes = new HashMap<>(original);
        attributes.put("app_user_id", user.getUserId()); // UUID 객체 유지
        attributes.put("provider_user_id", pId);
        attributes.put("is_new_user", isNewUser);
        return attributes;
    }

    private OAuthUserProfile normalizeProfile(OAuthUserProfile userProfile) {
        return OAuthUserProfile.builder()
                .email(userProfile.getEmail())
                .nickname(ensureNickname(userProfile.getNickname()))
                .profileImageUrl(userProfile.getProfileImageUrl())
                .birthDate(userProfile.getBirthDate())
                .build();
    }

    private record ResolvedOAuthUser(User user, boolean isNewUser) {
    }

    private AuthProvider mapProvider(String id) {
        if ("kakao".equalsIgnoreCase(id)) return AuthProvider.KAKAO;
        throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"));
    }

    protected OAuth2User loadProviderUser(OAuth2UserRequest userRequest) {
        return super.loadUser(userRequest);
    }
}
