package com.sogong.todak.auth.refresh.repository;

import com.sogong.todak.auth.refresh.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update RefreshToken rt
           set rt.revokedAt = :now
         where rt.tokenHash = :tokenHash
           and rt.revokedAt is null
        """)
    int revokeByTokenHash(@Param("tokenHash") String tokenHash,
                          @Param("now") OffsetDateTime now);

    // 운영 정리(선택)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from RefreshToken rt
         where rt.expiresAt < :cutoff
        """)
    int deleteExpiredBefore(@Param("cutoff") OffsetDateTime cutoff);

    long deleteByUserId(UUID userId);
}
