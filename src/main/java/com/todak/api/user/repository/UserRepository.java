package com.todak.api.user.repository;

import com.todak.api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /** kakaoId로 유저 찾기 */
    Optional<User> findByKakaoId(Long kakaoId);

    /** userUuid로 유저 찾기 (기본) */
    Optional<User> findByUserUuid(UUID uuid);
}
