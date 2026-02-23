package com.sogong.todak.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class UserSummaryResponse {

    private UUID userId;
    private String email;
    private String nickname;
    private LocalDate birthDate;          // ✅ 추가
    private String profileImageUrl;
    private List<String> providers;
}