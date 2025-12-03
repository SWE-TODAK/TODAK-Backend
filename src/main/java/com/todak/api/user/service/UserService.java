package com.todak.api.user.service;

import com.todak.api.user.entity.User;
import com.todak.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User loadOrCreateUser(Long userId, String name, String profileImage) {
        return userRepository.findByKakaoId(userId)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .kakaoId(userId)
                                .name(name)
                                .build()
                ));
    }

    public UUID getUserUuid(Long userId) {
        return userRepository.findByKakaoId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getUserUuid();
    }
}
