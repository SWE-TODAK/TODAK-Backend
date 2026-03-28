package com.sogong.todak.auth.service;

import com.sogong.todak.auth.domain.AuthProvider;
import com.sogong.todak.auth.dto.response.EmailAccountStatus;
import com.sogong.todak.auth.dto.response.EmailAccountStatusResponse;
import com.sogong.todak.user.entity.User;
import com.sogong.todak.user.entity.UserIdentity;
import com.sogong.todak.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailAccountStatusService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public EmailAccountStatusResponse getStatus(String rawEmail) {
        String email = normalizeEmail(rawEmail);

        return userRepository.findWithAuthAndIdentitiesByEmail(email)
                .map(user -> EmailAccountStatusResponse.builder()
                        .email(email)
                        .accountStatus(resolveStatus(user))
                        .providers(resolveProviders(user))
                        .active(!user.isDeleted())
                        .deleted(user.isDeleted())
                        .build())
                .orElseGet(() -> EmailAccountStatusResponse.builder()
                        .email(email)
                        .accountStatus(EmailAccountStatus.NEW_USER)
                        .providers(List.of())
                        .active(false)
                        .deleted(false)
                        .build());
    }

    private EmailAccountStatus resolveStatus(User user) {
        boolean local = user.hasAuth();
        boolean kakao = hasKakaoIdentity(user);

        if (!user.isDeleted()) {
            if (local) {
                return EmailAccountStatus.ACTIVE_LOCAL;
            }
            if (kakao) {
                return EmailAccountStatus.ACTIVE_KAKAO;
            }
        } else {
            if (local) {
                return EmailAccountStatus.DELETED_LOCAL;
            }
            if (kakao) {
                return EmailAccountStatus.DELETED_KAKAO;
            }
        }

        throw new IllegalStateException("지원하지 않는 계정 상태입니다.");
    }

    private List<String> resolveProviders(User user) {
        List<String> providers = new ArrayList<>();
        if (user.hasAuth()) {
            providers.add(AuthProvider.LOCAL.name());
        }
        user.getIdentities().stream()
                .map(UserIdentity::getProvider)
                .filter(provider -> provider == AuthProvider.KAKAO)
                .map(Enum::name)
                .distinct()
                .forEach(providers::add);
        return providers.stream().distinct().sorted().toList();
    }

    private boolean hasKakaoIdentity(User user) {
        return user.getIdentities().stream()
                .anyMatch(identity -> identity.getProvider() == AuthProvider.KAKAO);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
