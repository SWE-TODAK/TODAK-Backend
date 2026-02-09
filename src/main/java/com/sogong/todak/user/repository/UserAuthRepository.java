package com.sogong.todak.user.repository;

import com.sogong.todak.user.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, UUID> {

    /**
     * 특정 사용자의 인증 정보를 조회합니다.
     * UserAuth는 User와 PK를 공유(@MapsId)하므로
     * userId를 통해 바로 조회가 가능합니다.
     */
    Optional<UserAuth> findByUserId(UUID userId);

    /**
     * 특정 유저가 자체 로그인(Local) 정보를 가지고 있는지 확인합니다.
     */
    boolean existsByUserId(UUID userId);
}