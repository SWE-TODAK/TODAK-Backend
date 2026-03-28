package com.sogong.todak.auth.oauth2.service;

import com.sogong.todak.auth.domain.AuthProvider;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.entity.UserAuth;
import com.sogong.todak.user.entity.UserIdentity;
import com.sogong.todak.user.repository.UserAuthRepository;
import com.sogong.todak.user.repository.UserIdentityRepository;
import com.sogong.todak.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAuthRepository userAuthRepository;

    @Mock
    private UserIdentityRepository userIdentityRepository;

    @Test
    @DisplayName("카카오 신규 로그인 시 birthDate를 저장한다")
    void savesBirthDateForNewKakaoUser() {
        CustomOAuth2UserService customOAuth2UserService = serviceReturning(oauth2User("2001", "0523"));
        OAuth2UserRequest userRequest = kakaoUserRequest();

        when(userIdentityRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "12345")).thenReturn(Optional.empty());
        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNull("kakao@example.com")).thenReturn(Optional.empty());
        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNotNull("kakao@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> savedUser(invocation.getArgument(0)));
        when(userIdentityRepository.save(any(UserIdentity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customOAuth2UserService.loadUser(userRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(LocalDate.of(2001, 5, 23), userCaptor.getValue().getBirthDate());
    }

    @Test
    @DisplayName("기존 KAKAO 사용자는 재로그인 시 최신 카카오 프로필로 갱신된다")
    void updatesExistingKakaoUserWithLatestProfile() {
        CustomOAuth2UserService customOAuth2UserService = serviceReturning(oauth2User("2001", "0523"));
        OAuth2UserRequest userRequest = kakaoUserRequest();
        User existingUser = userWithBirthDate(LocalDate.of(1999, 1, 1));
        UserIdentity identity = UserIdentity.builder()
                .user(existingUser)
                .provider(AuthProvider.KAKAO)
                .providerUserId("12345")
                .providerEmail("kakao@example.com")
                .build();

        when(userIdentityRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "12345")).thenReturn(Optional.of(identity));

        customOAuth2UserService.loadUser(userRequest);

        assertEquals(LocalDate.of(2001, 5, 23), existingUser.getBirthDate());
        assertEquals("카카오유저", existingUser.getNickname());
        assertEquals("https://example.com/profile.png", existingUser.getProfileImageUrl());
    }

    @Test
    @DisplayName("카카오 birthday 또는 birthyear가 누락되면 방어적으로 처리한다")
    void handlesMissingBirthDateGracefully() {
        CustomOAuth2UserService customOAuth2UserService = serviceReturning(oauth2User("2001", null));
        OAuth2UserRequest userRequest = kakaoUserRequest();

        when(userIdentityRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "12345")).thenReturn(Optional.empty());
        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNull("kakao@example.com")).thenReturn(Optional.empty());
        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNotNull("kakao@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> savedUser(invocation.getArgument(0)));
        when(userIdentityRepository.save(any(UserIdentity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customOAuth2UserService.loadUser(userRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertNull(userCaptor.getValue().getBirthDate());
    }

    @Test
    @DisplayName("탈퇴한 KAKAO 유저는 같은 provider_user_id로 다시 로그인하면 복구된다")
    void restoresDeletedKakaoUserByIdentity() {
        CustomOAuth2UserService customOAuth2UserService = serviceReturning(oauth2User("2001", "0523"));
        OAuth2UserRequest userRequest = kakaoUserRequest();
        User deletedUser = userWithBirthDate(null);
        ReflectionTestUtils.setField(deletedUser, "deletedAt", OffsetDateTime.now());

        UserIdentity identity = UserIdentity.builder()
                .user(deletedUser)
                .provider(AuthProvider.KAKAO)
                .providerUserId("12345")
                .providerEmail("kakao@example.com")
                .build();

        when(userIdentityRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "12345")).thenReturn(Optional.of(identity));

        customOAuth2UserService.loadUser(userRequest);

        assertFalse(deletedUser.isDeleted());
        assertEquals(LocalDate.of(2001, 5, 23), deletedUser.getBirthDate());
    }

    @Test
    @DisplayName("탈퇴한 KAKAO 유저는 identity가 없어도 이메일로 찾아 복구한다")
    void restoresDeletedKakaoUserByEmail() {
        CustomOAuth2UserService customOAuth2UserService = serviceReturning(oauth2User("2001", "0523"));
        OAuth2UserRequest userRequest = kakaoUserRequest();
        User deletedUser = userWithBirthDate(null);
        ReflectionTestUtils.setField(deletedUser, "deletedAt", OffsetDateTime.now());

        when(userIdentityRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "12345")).thenReturn(Optional.empty());
        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNotNull("kakao@example.com")).thenReturn(Optional.of(deletedUser));
        when(userIdentityRepository.findByUser_UserIdAndProvider(deletedUser.getUserId(), AuthProvider.KAKAO)).thenReturn(Optional.empty());
        when(userIdentityRepository.save(any(UserIdentity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customOAuth2UserService.loadUser(userRequest);

        assertFalse(deletedUser.isDeleted());
        verify(userIdentityRepository).save(any(UserIdentity.class));
    }

    @Test
    @DisplayName("탈퇴한 KAKAO 유저는 providerUserId가 달라도 같은 이메일이면 기존 row를 복구하고 relink한다")
    void restoresDeletedKakaoUserByEmailWhenProviderUserIdChanged() {
        CustomOAuth2UserService customOAuth2UserService = serviceReturning(oauth2UserWith("99999", "kakao@example.com", "2001", "0523"));
        OAuth2UserRequest userRequest = kakaoUserRequest();
        User deletedUser = userWithBirthDate(null);
        ReflectionTestUtils.setField(deletedUser, "deletedAt", OffsetDateTime.now());

        UserIdentity oldIdentity = UserIdentity.builder()
                .user(deletedUser)
                .provider(AuthProvider.KAKAO)
                .providerUserId("12345")
                .providerEmail("old@example.com")
                .build();
        ReflectionTestUtils.setField(deletedUser, "identities", new ArrayList<>(List.of(oldIdentity)));

        when(userIdentityRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "99999")).thenReturn(Optional.empty());
        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNotNull("kakao@example.com")).thenReturn(Optional.of(deletedUser));
        when(userIdentityRepository.findByUser_UserIdAndProvider(deletedUser.getUserId(), AuthProvider.KAKAO))
                .thenReturn(Optional.of(oldIdentity));

        customOAuth2UserService.loadUser(userRequest);

        assertFalse(deletedUser.isDeleted());
        assertEquals("99999", oldIdentity.getProviderUserId());
        assertEquals("kakao@example.com", oldIdentity.getProviderEmail());
        assertEquals("카카오유저", deletedUser.getNickname());
        verify(userRepository, never()).findWithAuthAndIdentitiesByEmailAndDeletedAtIsNull("kakao@example.com");
    }

    @Test
    @DisplayName("카카오에서 이메일을 주지 않고 providerUserId도 매칭되지 않으면 신규 생성으로 진행한다")
    void createsNewUserWhenEmailMissingAndProviderUserIdDoesNotMatch() {
        CustomOAuth2UserService customOAuth2UserService = serviceReturning(oauth2UserWith("99999", null, "2001", "0523"));
        OAuth2UserRequest userRequest = kakaoUserRequest();

        when(userIdentityRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "99999")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> savedUser(invocation.getArgument(0)));
        when(userIdentityRepository.save(any(UserIdentity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customOAuth2UserService.loadUser(userRequest);

        verify(userRepository).save(any(User.class));
        verify(userRepository, never()).findWithAuthAndIdentitiesByEmailAndDeletedAtIsNotNull(any());
        verify(userRepository, never()).findWithAuthAndIdentitiesByEmailAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("활성 계정이 같은 이메일로 존재하면 active_email_exists로 막힌다")
    void throwsWhenActiveUserExistsWithSameEmail() {
        CustomOAuth2UserService customOAuth2UserService = serviceReturning(oauth2User("2001", "0523"));
        OAuth2UserRequest userRequest = kakaoUserRequest();
        User activeUser = userWithBirthDate(null);

        when(userIdentityRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "12345")).thenReturn(Optional.empty());
        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNotNull("kakao@example.com")).thenReturn(Optional.empty());
        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNull("kakao@example.com")).thenReturn(Optional.of(activeUser));

        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> customOAuth2UserService.loadUser(userRequest)
        );

        assertEquals("active_email_exists", exception.getError().getErrorCode());
    }

    @Test
    @DisplayName("삭제된 KAKAO 계정이 복구 가능하면 active 조회보다 먼저 복구를 시도한다")
    void restoresDeletedKakaoBeforeCheckingActiveEmail() {
        CustomOAuth2UserService customOAuth2UserService = serviceReturning(oauth2UserWith("99999", "kakao@example.com", "2001", "0523"));
        OAuth2UserRequest userRequest = kakaoUserRequest();
        User deletedUser = userWithBirthDate(null);
        ReflectionTestUtils.setField(deletedUser, "deletedAt", OffsetDateTime.now());

        UserIdentity oldIdentity = UserIdentity.builder()
                .user(deletedUser)
                .provider(AuthProvider.KAKAO)
                .providerUserId("12345")
                .providerEmail("old@example.com")
                .build();
        ReflectionTestUtils.setField(deletedUser, "identities", new ArrayList<>(List.of(oldIdentity)));

        when(userIdentityRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "99999")).thenReturn(Optional.empty());
        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNotNull("kakao@example.com")).thenReturn(Optional.of(deletedUser));
        when(userIdentityRepository.findByUser_UserIdAndProvider(deletedUser.getUserId(), AuthProvider.KAKAO))
                .thenReturn(Optional.of(oldIdentity));

        customOAuth2UserService.loadUser(userRequest);

        verify(userRepository, never()).findWithAuthAndIdentitiesByEmailAndDeletedAtIsNull("kakao@example.com");
    }

    @Test
    @DisplayName("탈퇴한 LOCAL 유저가 KAKAO로 재진입하면 기존 auth를 제거하고 KAKAO 기준으로 재가입처럼 갱신한다")
    void resetsDeletedLocalUserForKakaoSignup() {
        CustomOAuth2UserService customOAuth2UserService = serviceReturning(oauth2User("2001", "0523"));
        OAuth2UserRequest userRequest = kakaoUserRequest();
        User deletedLocalUser = userWithBirthDate(LocalDate.of(1998, 8, 8));
        ReflectionTestUtils.setField(deletedLocalUser, "deletedAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(deletedLocalUser, "gender", com.sogong.todak.user.entity.Gender.MALE);

        UserAuth userAuth = UserAuth.builder()
                .user(deletedLocalUser)
                .passwordHash("encoded-password")
                .build();
        ReflectionTestUtils.setField(deletedLocalUser, "auth", userAuth);

        when(userIdentityRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "12345")).thenReturn(Optional.empty());
        when(userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNotNull("kakao@example.com")).thenReturn(Optional.of(deletedLocalUser));
        when(userAuthRepository.findByUserId(deletedLocalUser.getUserId())).thenReturn(Optional.of(userAuth));
        when(userIdentityRepository.findByUser_UserIdAndProvider(deletedLocalUser.getUserId(), AuthProvider.KAKAO)).thenReturn(Optional.empty());
        when(userIdentityRepository.save(any(UserIdentity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customOAuth2UserService.loadUser(userRequest);

        assertFalse(deletedLocalUser.isDeleted());
        assertEquals("카카오유저", deletedLocalUser.getNickname());
        assertEquals(LocalDate.of(2001, 5, 23), deletedLocalUser.getBirthDate());
        assertNull(deletedLocalUser.getGender());
        assertNull(deletedLocalUser.getAuth());
        verify(userAuthRepository).delete(userAuth);
        verify(userIdentityRepository).save(any(UserIdentity.class));
    }

    private OAuth2UserRequest kakaoUserRequest() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("kakao")
                .clientId("client-id")
                .clientSecret("client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .scope("profile_nickname", "profile_image", "account_email", "birthday", "birthyear")
                .clientName("Kakao")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        return new OAuth2UserRequest(registration, accessToken);
    }

    private OAuth2User oauth2User(String birthyear, String birthday) {
        return oauth2UserWith("12345", "kakao@example.com", birthyear, birthday);
    }

    private OAuth2User oauth2UserWith(String providerId, String email, String birthyear, String birthday) {
        Map<String, Object> kakaoAccount = new java.util.HashMap<>();
        if (email != null) {
            kakaoAccount.put("email", email);
        }
        kakaoAccount.put("profile", Map.of(
                "nickname", "카카오유저",
                "profile_image_url", "https://example.com/profile.png"
        ));
        if (birthyear != null) {
            kakaoAccount.put("birthyear", birthyear);
        }
        if (birthday != null) {
            kakaoAccount.put("birthday", birthday);
        }

        return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                List.of(),
                Map.of(
                        "id", Long.parseLong(providerId),
                        "kakao_account", kakaoAccount
                ),
                "id"
        );
    }

    private User userWithBirthDate(LocalDate birthDate) {
        User user = User.builder()
                .email("kakao@example.com")
                .nickname("기존유저")
                .profileImageUrl("https://example.com/old.png")
                .birthDate(birthDate)
                .build();
        ReflectionTestUtils.setField(user, "userId", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "identities", new ArrayList<>());
        return user;
    }

    private CustomOAuth2UserService serviceReturning(OAuth2User oauth2User) {
        return new CustomOAuth2UserService(userRepository, userAuthRepository, userIdentityRepository) {
            @Override
            protected OAuth2User loadProviderUser(OAuth2UserRequest userRequest) {
                return oauth2User;
            }
        };
    }

    private User savedUser(User user) {
        ReflectionTestUtils.setField(user, "userId", UUID.randomUUID());
        return user;
    }
}
