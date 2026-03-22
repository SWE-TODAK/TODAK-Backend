package com.sogong.todak.user.service.impl;

import com.sogong.todak.auth.domain.AuthProvider;
import com.sogong.todak.common.exception.DuplicateResourceException;
import com.sogong.todak.user.dto.request.UpdateBirthRequest;
import com.sogong.todak.user.dto.request.UpdateEmailRequest;
import com.sogong.todak.user.dto.request.UpdateGenderRequest;
import com.sogong.todak.user.dto.request.UpdateNicknameRequest;
import com.sogong.todak.user.dto.request.UpdateProfileImageRequest;
import com.sogong.todak.user.dto.response.UserMeProfileResponse;
import com.sogong.todak.user.dto.response.UserMeResponse;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.repository.UserRepository;
import com.sogong.todak.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public void updateNickname(UpdateNicknameRequest request) {
        User user = getCurrentUserForUpdate();

        String nickname = normalizeNickname(request.getNickname());
        String currentNickname = normalizeNickname(user.getNickname());

        if (!Objects.equals(nickname, currentNickname)
                && userRepository.existsByNickname(nickname)) {
            throw new DuplicateResourceException("이미 존재하는 닉네임입니다.");
        }

        user.updateNickname(nickname);
    }

    @Override
    @Transactional
    public void updateEmail(UpdateEmailRequest request) {
        User user = getCurrentUserForUpdate();

        String email = normalizeEmail(request.getEmail());
        String currentEmail = normalizeEmail(user.getEmail());

        if (!Objects.equals(email, currentEmail)
                && userRepository.existsByEmail(email)) {
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

    private User getCurrentUser() {
        UUID userId = extractCurrentUserId();
        return userRepository.findWithAuthAndIdentitiesByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private User getCurrentUserForUpdate() {
        UUID userId = extractCurrentUserId();
        return userRepository.findByUserId(userId)
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
}
