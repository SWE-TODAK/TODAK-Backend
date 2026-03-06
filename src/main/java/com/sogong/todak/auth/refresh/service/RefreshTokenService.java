package com.sogong.todak.auth.refresh.service;

import com.sogong.todak.auth.refresh.service.RotateResult;
import com.sogong.todak.auth.refresh.entity.RefreshToken;
import com.sogong.todak.auth.refresh.policy.RefreshTokenPolicy;
import com.sogong.todak.auth.refresh.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenPolicy refreshTokenPolicy;

    /**
     * 로그인/회원가입 시 Refresh 발급
     * DB에는 hash만 저장하고 raw는 클라이언트에 반환
     */
    public String issue(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        String rawToken = refreshTokenPolicy.generateRawToken();
        String tokenHash = refreshTokenPolicy.hash(rawToken);

        RefreshToken refreshToken = RefreshToken.issue(
                userId,
                tokenHash,
                refreshTokenPolicy.expiresAtFromNow()
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken; // 클라이언트에 전달
    }

    /**
     * Refresh Token Rotation (RTR)
     * old revoke + new 발급
     */
    public RotateResult rotate(String rawRefreshToken) {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String tokenHash = refreshTokenPolicy.hash(rawRefreshToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("INVALID_REFRESH_TOKEN"));

        // 만료 체크
        if (stored.isExpired(now)) {
            throw new IllegalArgumentException("EXPIRED_REFRESH_TOKEN");
        }

        // 이미 revoke된 경우 (정책: only 401)
        if (stored.isRevoked()) {
            throw new IllegalArgumentException("REVOKED_REFRESH_TOKEN");
        }

        // RTR: old revoke
        int updated = refreshTokenRepository.revokeByTokenHash(tokenHash, now);
        if (updated == 0) {
            // 경쟁상황(이미 revoke됨 등)
            throw new IllegalArgumentException("REVOKED_REFRESH_TOKEN");
        }

        // new refresh 발급
        String newRawToken = refreshTokenPolicy.generateRawToken();
        String newHash = refreshTokenPolicy.hash(newRawToken);

        RefreshToken newRefresh = RefreshToken.issue(
                stored.getUserId(),
                newHash,
                refreshTokenPolicy.expiresAtFromNow()
        );

        refreshTokenRepository.save(newRefresh);

        return new RotateResult(stored.getUserId(), newRawToken);
    }

    /**
     * 로그아웃 (현 기기만 지원)
     * idempotent 처리: 이미 revoke거나 없어도 에러 안 던짐
     */
    public void revoke(String rawRefreshToken) {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String tokenHash = refreshTokenPolicy.hash(rawRefreshToken);

        refreshTokenRepository.revokeByTokenHash(tokenHash, now);
    }
}