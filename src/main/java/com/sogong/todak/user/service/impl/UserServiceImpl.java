package com.sogong.todak.user.service.impl;

import com.sogong.todak.auth.domain.AuthProvider;
import com.sogong.todak.auth.refresh.service.RefreshTokenService;
import com.sogong.todak.common.exception.DuplicateResourceException;
import com.sogong.todak.common.exception.InvalidPasswordException;
import com.sogong.todak.common.exception.UnsupportedAuthProviderException;
import com.sogong.todak.user.dto.request.ChangePasswordRequest;
import com.sogong.todak.user.dto.request.UpdateBirthRequest;
import com.sogong.todak.user.dto.request.UpdateEmailRequest;
import com.sogong.todak.user.dto.request.UpdateGenderRequest;
import com.sogong.todak.user.dto.request.UpdateNicknameRequest;
import com.sogong.todak.user.dto.request.UpdateProfileImageRequest;
import com.sogong.todak.user.dto.response.PasswordChangeResponse;
import com.sogong.todak.user.dto.response.UserMeProfileResponse;
import com.sogong.todak.user.dto.response.UserMeResponse;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.repository.UserRepository;
import com.sogong.todak.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Override
    public UserMeResponse getMyPage() {
        User user = getCurrentUser();

        return UserMeResponse.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    @Override
    public UserMeProfileResponse getMyProfile() {
        User user = getCurrentUser();

        boolean kakaoLinked = user.getIdentities().stream()
                .anyMatch(identity -> identity.getProvider() == AuthProvider.KAKAO);

        boolean hasPassword = user.getAuth() != null;

        return UserMeProfileResponse.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .profileImageUrl(user.getProfileImageUrl())
                .kakaoLinked(kakaoLinked)
                .hasPassword(hasPassword)
                .build();
    }

    @Override
    @Transactional
    public void updateProfileImage(UpdateProfileImageRequest request) {
        User user = getCurrentUserForUpdate();
        user.updateProfileImageUrl(request.getProfileImageUrl());
    }

    @Override
    @Transactional
    public void deleteProfileImage() {
        User user = getCurrentUserForUpdate();
        user.removeProfileImageUrl();
    }

    @Override
    @Transactional
    public void updateNickname(UpdateNicknameRequest request) {
        User user = getCurrentUserForUpdate();

        String nickname = normalizeNickname(request.getNickname());
        user.updateNickname(nickname);
    }

    @Override
    @Transactional
    public void updateEmail(UpdateEmailRequest request) {
        User user = getCurrentUserForUpdate();

        String email = normalizeEmail(request.getEmail());
        String currentEmail = normalizeEmail(user.getEmail());

        if (!Objects.equals(email, currentEmail)
                && userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new DuplicateResourceException("이미 존재하는 이메일입니다.");
        }

        user.updateEmail(email);
    }

    @Override
    @Transactional
    public void updateBirthDate(UpdateBirthRequest request) {
        User user = getCurrentUserForUpdate();
        user.updateBirthDate(request.getBirthDate());
    }

    @Override
    @Transactional
    public void updateGender(UpdateGenderRequest request) {
        User user = getCurrentUserForUpdate();
        user.updateGender(request.getGender());
    }

    @Override
    @Transactional
    public PasswordChangeResponse changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (user.getAuth() == null) {
            throw new UnsupportedAuthProviderException("로컬 비밀번호가 없는 계정입니다.");
        }

        validatePasswordChangeRequest(user, request);

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.getAuth().updatePassword(encodedNewPassword);
        refreshTokenService.removeAllByUserId(user.getUserId());

        return PasswordChangeResponse.builder()
                .message("비밀번호가 변경되었습니다. 다시 로그인해주세요.")
                .build();
    }

    private User getCurrentUser() {
        UUID userId = extractCurrentUserId();
        return userRepository.findWithAuthAndIdentitiesByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private User getCurrentUserForUpdate() {
        UUID userId = extractCurrentUserId();
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private UUID extractCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("인증 정보가 유효하지 않습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UUID userId) {
            return userId;
        }

        if (principal instanceof String principalString) {
            return parseUuid(principalString);
        }

        return parseUuid(authentication.getName());
    }

    private UUID parseUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (Exception e) {
            throw new IllegalArgumentException("인증 사용자 식별자 형식이 올바르지 않습니다.");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNickname(String nickname) {
        return nickname == null ? null : nickname.trim();
    }

    private void validatePasswordChangeRequest(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getAuth().getPasswordHash())) {
            throw new InvalidPasswordException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new InvalidPasswordException("새 비밀번호는 현재 비밀번호와 동일할 수 없습니다.");
        }
    }
}
