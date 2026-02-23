package com.sogong.todak.user.repository;

import com.sogong.todak.auth.domain.AuthProvider;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.entity.UserIdentity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, UUID> {

    /**
     * OAuth2 로그인에서 가장 많이 쓰는 조회:
     * (provider, providerUserId)로 찾고 user까지 같이 로딩.
     */
    @EntityGraph(attributePaths = {"user"})
    Optional<UserIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    boolean existsByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    /**
     * 특정 유저에 연결된 모든 인증 수단 조회 (계정 관리 페이지 등)
     */
    List<UserIdentity> findAllByUser(User user);

    // ✅ (개선) userId로 바로 목록 조회: 서비스에서 User를 또 조회할 필요 없음
    List<UserIdentity> findAllByUser_UserId(UUID userId);

    /**
     * 특정 유저가 특정 provider를 이미 연결했는지 확인 (계정 연동 로직)
     */
    boolean existsByUser_UserIdAndProvider(UUID userId, AuthProvider provider);

    // ✅ (개선) 특정 유저의 특정 provider identity를 직접 조회 (연동 해제/갱신 시 유용)
    Optional<UserIdentity> findByUser_UserIdAndProvider(UUID userId, AuthProvider provider);
}