package com.sogong.todak.user.repository;

import com.sogong.todak.user.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, UUID> {

    Optional<UserAuth> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    // ✅ 로컬 로그인 핵심: email로 UserAuth 바로 조회 (쿼리 1번으로 끝)
    Optional<UserAuth> findByUser_Email(String email);

    // ✅ (선택) 로컬 계정 존재 여부를 email로도 확인하고 싶을 때
    boolean existsByUser_Email(String email);
}