package com.sogong.todak.auth.service;

import com.sogong.todak.auth.domain.AuthProvider;
import com.sogong.todak.auth.dto.response.EmailAccountStatus;
import com.sogong.todak.auth.dto.response.EmailAccountStatusResponse;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.entity.UserAuth;
import com.sogong.todak.user.entity.UserIdentity;
import com.sogong.todak.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailAccountStatusServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("존재하지 않는 이메일은 NEW_USER를 반환한다")
    void returnsNewUserWhenEmailDoesNotExist() {
        EmailAccountStatusService service = new EmailAccountStatusService(userRepository);
        when(userRepository.findWithAuthAndIdentitiesByEmail("new@example.com")).thenReturn(Optional.empty());

        EmailAccountStatusResponse response = service.getStatus("new@example.com");

        assertEquals("new@example.com", response.getEmail());
        assertEquals(EmailAccountStatus.NEW_USER, response.getAccountStatus());
        assertEquals(List.of(), response.getProviders());
        assertFalse(response.isActive());
        assertFalse(response.isDeleted());
    }

    @Test
    @DisplayName("활성 LOCAL 계정은 ACTIVE_LOCAL을 반환한다")
    void returnsActiveLocal() {
        EmailAccountStatusService service = new EmailAccountStatusService(userRepository);
        User user = localUser(false);
        when(userRepository.findWithAuthAndIdentitiesByEmail("local@example.com")).thenReturn(Optional.of(user));

        EmailAccountStatusResponse response = service.getStatus("local@example.com");

        assertEquals(EmailAccountStatus.ACTIVE_LOCAL, response.getAccountStatus());
        assertEquals(List.of("LOCAL"), response.getProviders());
        assertTrue(response.isActive());
        assertFalse(response.isDeleted());
    }

    @Test
    @DisplayName("활성 KAKAO 계정은 ACTIVE_KAKAO를 반환한다")
    void returnsActiveKakao() {
        EmailAccountStatusService service = new EmailAccountStatusService(userRepository);
        User user = kakaoUser(false);
        when(userRepository.findWithAuthAndIdentitiesByEmail("kakao@example.com")).thenReturn(Optional.of(user));

        EmailAccountStatusResponse response = service.getStatus("kakao@example.com");

        assertEquals(EmailAccountStatus.ACTIVE_KAKAO, response.getAccountStatus());
        assertEquals(List.of("KAKAO"), response.getProviders());
        assertTrue(response.isActive());
        assertFalse(response.isDeleted());
    }

    @Test
    @DisplayName("탈퇴한 LOCAL 계정은 DELETED_LOCAL을 반환한다")
    void returnsDeletedLocal() {
        EmailAccountStatusService service = new EmailAccountStatusService(userRepository);
        User user = localUser(true);
        when(userRepository.findWithAuthAndIdentitiesByEmail("deleted-local@example.com")).thenReturn(Optional.of(user));

        EmailAccountStatusResponse response = service.getStatus("deleted-local@example.com");

        assertEquals(EmailAccountStatus.DELETED_LOCAL, response.getAccountStatus());
        assertEquals(List.of("LOCAL"), response.getProviders());
        assertFalse(response.isActive());
        assertTrue(response.isDeleted());
    }

    @Test
    @DisplayName("탈퇴한 KAKAO 계정은 DELETED_KAKAO를 반환한다")
    void returnsDeletedKakao() {
        EmailAccountStatusService service = new EmailAccountStatusService(userRepository);
        User user = kakaoUser(true);
        when(userRepository.findWithAuthAndIdentitiesByEmail("deleted-kakao@example.com")).thenReturn(Optional.of(user));

        EmailAccountStatusResponse response = service.getStatus("deleted-kakao@example.com");

        assertEquals(EmailAccountStatus.DELETED_KAKAO, response.getAccountStatus());
        assertEquals(List.of("KAKAO"), response.getProviders());
        assertFalse(response.isActive());
        assertTrue(response.isDeleted());
    }

    private User localUser(boolean deleted) {
        User user = User.builder()
                .email(deleted ? "deleted-local@example.com" : "local@example.com")
                .nickname("local-user")
                .build();
        ReflectionTestUtils.setField(user, "userId", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "identities", new ArrayList<>());

        UserAuth userAuth = UserAuth.builder()
                .user(user)
                .passwordHash("encoded-password")
                .build();
        ReflectionTestUtils.setField(user, "auth", userAuth);

        if (deleted) {
            ReflectionTestUtils.setField(user, "deletedAt", OffsetDateTime.now());
        }
        return user;
    }

    private User kakaoUser(boolean deleted) {
        User user = User.builder()
                .email(deleted ? "deleted-kakao@example.com" : "kakao@example.com")
                .nickname("kakao-user")
                .build();
        ReflectionTestUtils.setField(user, "userId", UUID.randomUUID());

        UserIdentity identity = UserIdentity.builder()
                .user(user)
                .provider(AuthProvider.KAKAO)
                .providerUserId("12345")
                .providerEmail(user.getEmail())
                .build();
        ReflectionTestUtils.setField(user, "identities", new ArrayList<>(List.of(identity)));

        if (deleted) {
            ReflectionTestUtils.setField(user, "deletedAt", OffsetDateTime.now());
        }
        return user;
    }
}
