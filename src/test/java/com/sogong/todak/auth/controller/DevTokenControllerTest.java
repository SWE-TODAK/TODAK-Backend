package com.sogong.todak.auth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
class DevTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("개발용 토큰 발급 API 성공 테스트")
    void getDevTokenSuccess() throws Exception {
        // given
        Long userId = 100L;

        // when & then
        mockMvc.perform(get("/auth/dev-token")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("userId가 없으면 기본값 1로 토큰 발급")
    void getDevTokenDefault() throws Exception {
        // when & then
        mockMvc.perform(get("/auth/dev-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
    }
}