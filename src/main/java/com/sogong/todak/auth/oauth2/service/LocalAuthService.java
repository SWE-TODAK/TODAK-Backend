package com.sogong.todak.auth.oauth2.service;

import com.sogong.todak.auth.domain.AuthProvider;
import com.sogong.todak.auth.dto.request.LoginRequest;
import com.sogong.todak.auth.dto.request.SignupRequest;
import com.sogong.todak.auth.dto.response.AuthResult;
import com.sogong.todak.auth.dto.response.AuthResponse;
import com.sogong.todak.auth.dto.response.TokenPairResponse;
import com.sogong.todak.auth.dto.response.UserSummaryResponse;
import com.sogong.todak.auth.jwt.JwtTokenProvider;
import com.sogong.todak.auth.refresh.service.RefreshTokenService;
import com.sogong.todak.common.exception.DuplicateResourceException;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.entity.UserAuth;
import com.sogong.todak.user.repository.UserAuthRepository;
import com.sogong.todak.user.repository.UserIdentityRepository;
import com.sogong.todak.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocalAuthService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    /**
     * 로컬 회원가입: User + UserAuth 생성 후 즉시 토큰 발급
     */
    @Transactional
    public AuthResponse signup(SignupRequest req) {
        String email = normalizeEmail(req.getEmail());
        String nickname = normalizeNickname(req.getNickname());

        Optional<User> activeUser = userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNull(email);
        if (activeUser.isPresent()) {
            throw new DuplicateResourceException("이미 존재하는 이메일입니다.");
        }

        Optional<User> deletedUser = userRepository.findWithAuthAndIdentitiesByEmailAndDeletedAtIsNotNull(email);
        AuthResult authResult;
        User user;
        if (deletedUser.isPresent()) {
            RestoreResult restoreResult = restoreDeletedUserForLocalSignup(deletedUser.get(), req, email, nickname);
            user = restoreResult.user();
            authResult = restoreResult.authResult();
        } else {
            user = createNewLocalUser(req, email, nickname);
            authResult = AuthResult.LOCAL_SIGNED_UP;
        }

        TokenPairResponse token = issueTokenPair(user.getUserId());
        UserSummaryResponse userSummary = buildUserSummary(user);

        return AuthResponse.builder()
                .authResult(authResult)
                .token(token)
                .user(userSummary)
                .build();
    }

    /**
     * 로컬 로그인: email/password 검증 후 토큰 발급
     *
     * 주의: refresh 발급(INSERT)이 발생하므로 readOnly=true면 안 됨
     */
    @Transactional
    public AuthResponse login(LoginRequest req) {
        String email = normalizeEmail(req.getEmail());

        UserAuth userAuth = userAuthRepository.findByUser_EmailAndUser_DeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(req.getPassword(), userAuth.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        User user = userAuth.getUser();
        TokenPairResponse token = issueTokenPair(user.getUserId());
        UserSummaryResponse userSummary = buildUserSummary(user);

        return AuthResponse.builder()
                .authResult(AuthResult.LOCAL_LOGGED_IN)
                .token(token)
                .user(userSummary)
                .build();
    }

    private User createNewLocalUser(SignupRequest req, String email, String nickname) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .birthDate(req.getBirthDate())
                .gender(req.getGender())
                .build();

        user = userRepository.save(user);
        createOrUpdateLocalAuth(user, req.getPassword());
        return user;
    }

    private RestoreResult restoreDeletedUserForLocalSignup(User deletedUser, SignupRequest req, String email, String nickname) {
        if (deletedUser.hasAuth()) {
            deletedUser.updateLocalProfile(email, nickname, req.getBirthDate(), req.getGender());
            deletedUser.restore();
            createOrUpdateLocalAuth(deletedUser, req.getPassword());
            return new RestoreResult(deletedUser, AuthResult.LOCAL_RESTORED);
        }

        deletedUser.replaceWithLocalProfile(email, nickname, req.getBirthDate(), req.getGender());
        deletedUser.restore();
        createOrUpdateLocalAuth(deletedUser, req.getPassword());
        userIdentityRepository.findByUser_UserIdAndProvider(deletedUser.getUserId(), AuthProvider.KAKAO)
                .ifPresent(userIdentityRepository::delete);
        return new RestoreResult(deletedUser, AuthResult.LOCAL_CONVERTED);
    }

    private void createOrUpdateLocalAuth(User user, String rawPassword) {
        String hashed = passwordEncoder.encode(rawPassword);
        userAuthRepository.findByUserId(user.getUserId())
                .ifPresentOrElse(
                        auth -> auth.updatePassword(hashed),
                        () -> userAuthRepository.save(UserAuth.builder()
                                .user(user)
                                .passwordHash(hashed)
                                .build())
                );
    }

    // =========================
    // 내부 헬퍼들
    // =========================

    private TokenPairResponse issueTokenPair(UUID userId) {
        String access = jwtTokenProvider.createAccessToken(userId, "ROLE_USER");
        String refreshRaw = refreshTokenService.issue(userId);

        return TokenPairResponse.builder()
                .tokenType("Bearer")
                .accessToken(access)
                .refreshToken(refreshRaw)
                .expiresInSeconds(jwtTokenProvider.getAccessExpiresInSeconds())
                .rotated(true) // ✅ 새 refresh 발급(클라 저장 강제 신호)
                .build();
    }

    private UserSummaryResponse buildUserSummary(User user) {
        List<String> providers = new ArrayList<>();

        // LOCAL은 user_auth 존재로 판단 (지금은 로컬 가입/로그인 흐름이니 무조건 포함)
        providers.add(AuthProvider.LOCAL.name());

        // 연결된 소셜 providers 추가
        var identities = userIdentityRepository.findAllByUser_UserId(user.getUserId());

        providers.addAll(
                identities.stream()
                        .map(i -> i.getProvider().name())
                        .distinct()
                        .collect(Collectors.toList())
        );

        providers = providers.stream().distinct().sorted().toList();

        return UserSummaryResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .profileImageUrl(user.getProfileImageUrl())
                .providers(providers)
                .build();
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNickname(String nickname) {
        if (nickname == null) return null;
        return nickname.trim();
    }

    private record RestoreResult(User user, AuthResult authResult) {
    }

}
