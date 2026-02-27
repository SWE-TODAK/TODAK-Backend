package com.sogong.todak.auth.refresh.service;

import java.util.UUID;

public record RotateResult(
        UUID userId,
        String newRefreshToken
) {}