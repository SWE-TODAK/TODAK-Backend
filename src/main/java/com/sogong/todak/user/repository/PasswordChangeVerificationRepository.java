package com.sogong.todak.user.repository;

import com.sogong.todak.user.entity.PasswordChangeVerification;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PasswordChangeVerificationRepository extends JpaRepository<PasswordChangeVerification, UUID> {

    Optional<PasswordChangeVerification> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select verification
          from PasswordChangeVerification verification
         where verification.userId = :userId
        """)
    Optional<PasswordChangeVerification> findByUserIdForUpdate(@Param("userId") UUID userId);
}
