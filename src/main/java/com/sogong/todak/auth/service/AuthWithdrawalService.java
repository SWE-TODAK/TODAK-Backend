package com.sogong.todak.auth.service;

import com.sogong.todak.auth.domain.AuthProvider;
import com.sogong.todak.auth.dto.request.WithdrawRequest;
import com.sogong.todak.auth.refresh.service.RefreshTokenService;
import com.sogong.todak.common.exception.AlreadyWithdrawnUserException;
import com.sogong.todak.common.exception.InvalidPasswordException;
import com.sogong.todak.common.exception.UnsupportedAuthProviderException;
import com.sogong.todak.common.exception.UserNotFoundException;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.entity.UserIdentity;
import com.sogong.todak.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthWithdrawalService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KakaoUnlinkService kakaoUnlinkService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void withdrawCurrentUser(WithdrawRequest request) {
        UUID userId = extractCurrentUserId();
        User user = getCurrentActiveUser(userId);

        AuthProvider provider = resolveProvider(user);
        if (provider == AuthProvider.LOCAL) {
            validateLocalPassword(user, request);
        } else if (provider == AuthProvider.KAKAO) {
            unlinkKakao(user);
        } else {
            throw new UnsupportedAuthProviderException("지원하지 않는 로그인 방식입니다.");
        }

        user.softDelete();
        refreshTokenService.removeAllByUserId(user.getUserId());
        SecurityContextHolder.clearContext();
    }

    private User getCurrentActiveUser(UUID userId) {
        return userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> {
                    User user = userRepository.findWithAuthAndIdentitiesByUserId(userId)
                            .orElseThrow(() -> new UserNotFoundException("탈퇴할 사용자를 찾을 수 없습니다."));

                    if (user.isDeleted()) {
                        throw new AlreadyWithdrawnUserException("이미 탈퇴한 사용자입니다.");
                    }

                    throw new UserNotFoundException("탈퇴할 사용자를 찾을 수 없습니다.");
                });
    }

    private AuthProvider resolveProvider(User user) {
        if (user.getAuth() != null) {
            return AuthProvider.LOCAL;
        }

        boolean kakaoLinked = user.getIdentities().stream()
                .anyMatch(identity -> identity.getProvider() == AuthProvider.KAKAO);

        if (kakaoLinked) {
            return AuthProvider.KAKAO;
        }

        throw new UnsupportedAuthProviderException("지원하지 않는 로그인 방식입니다.");
    }

    private void validateLocalPassword(User user, WithdrawRequest request) {
        String password = request == null ? null : request.getPassword();
        if (!StringUtils.hasText(password)) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }

        if (user.getAuth() == null || !passwordEncoder.matches(password, user.getAuth().getPasswordHash())) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }
    }

    private void unlinkKakao(User user) {
        UserIdentity kakaoIdentity = user.getIdentities().stream()
                .filter(identity -> identity.getProvider() == AuthProvider.KAKAO)
                .findFirst()
                .orElseThrow(() -> new UnsupportedAuthProviderException("카카오 계정 정보를 찾을 수 없습니다."));

        kakaoUnlinkService.unlink(kakaoIdentity.getProviderUserId());
    }

    private UUID extractCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getPrincipal() == null) {
            throw new UserNotFoundException("탈퇴할 사용자를 찾을 수 없습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UUID userId) {
            return userId;
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (Exception e) {
            throw new UserNotFoundException("탈퇴할 사용자를 찾을 수 없습니다.");
        }
    }
}
