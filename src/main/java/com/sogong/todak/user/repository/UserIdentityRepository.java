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
     * @EntityGraph를 사용하여 User 엔티티까지 한 번에 페치 조인합니다. (N+1 방지)
     */
    @EntityGraph(attributePaths = {"user"})
    Optional<UserIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    boolean existsByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    /**
     * 특정 유저에 연결된 모든 인증 수단 조회 (계정 관리 페이지 등)
     */
    List<UserIdentity> findAllByUser(User user);

    /**
     * 특정 유저가 특정 provider를 이미 연결했는지 확인 (계정 연동 로직)
     * 파라미터를 객체(User)가 아닌 ID(UUID)로 받는 버전도 유지합니다.
     */
    boolean existsByUser_UserIdAndProvider(UUID userId, AuthProvider provider);
}