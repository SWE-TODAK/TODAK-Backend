package com.sogong.todak.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sogong.todak.common.exception.GlobalExceptionHandler;
import com.sogong.todak.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);

        UserController controller = new UserController(userService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("프로필 이미지 수정 요청은 204를 반환한다")
    void updateProfileImageReturnsNoContent() throws Exception {
        Map<String, Object> request = Map.of(
                "profileImageUrl", "https://cdn.todak.com/profile/tester.png"
        );

        mockMvc.perform(patch("/api/v1/users/me/profile/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).updateProfileImage(any());
    }

    @Test
    @DisplayName("프로필 이미지 삭제 요청은 204를 반환한다")
    void deleteProfileImageReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me/profile/image"))
                .andExpect(status().isNoContent());

        verify(userService).deleteProfileImage();
    }
}
