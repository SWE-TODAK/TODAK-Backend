package com.sogong.todak.user.repository;

import com.sogong.todak.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * 이메일로 사용자 조회
     * 로컬 로그인 및 이메일 기반 계정 찾기에 사용됩니다.
     */
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByUserId(UUID userId);

    Optional<User> findByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByUserIdAndDeletedAtIsNull(UUID userId);

    @EntityGraph(attributePaths = {"auth", "identities"})
    Optional<User> findWithAuthAndIdentitiesByUserId(UUID userId);

    @EntityGraph(attributePaths = {"auth", "identities"})
    Optional<User> findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(UUID userId);
}
