package com.sogong.todak.user.repository;

import com.sogong.todak.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * 닉네임 중복 체크 (회원가입/프로필 수정 시 필수)
     */
    boolean existsByNickname(String nickname);

    /**
     * 이메일로 사용자 조회
     * 로컬 로그인 및 이메일 기반 계정 찾기에 사용됩니다.
     */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * (추가 제안) 닉네임으로 사용자 조회
     */
    Optional<User> findByNickname(String nickname);
}