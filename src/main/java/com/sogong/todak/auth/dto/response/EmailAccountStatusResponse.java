package com.sogong.todak.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EmailAccountStatusResponse {

    private String email;
    private EmailAccountStatus accountStatus;
    private List<String> providers;
    private boolean active;
    private boolean deleted;
}
