package com.sogong.todak.auth.jwt;

import com.sogong.todak.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("활성 사용자 토큰은 SecurityContext에 인증 정보를 저장한다")
    void authenticatesActiveUser() throws ServletException, IOException {
        UUID userId = UUID.randomUUID();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider, userRepository);

        when(jwtTokenProvider.validate("access-token")).thenReturn(true);
        when(jwtTokenProvider.getUserId("access-token")).thenReturn(userId);
        when(jwtTokenProvider.getRole("access-token")).thenReturn("ROLE_USER");
        when(userRepository.existsByUserIdAndDeletedAtIsNull(userId)).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("탈퇴한 사용자 토큰은 인증되지 않는다")
    void doesNotAuthenticateWithdrawnUser() throws ServletException, IOException {
        UUID userId = UUID.randomUUID();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider, userRepository);

        when(jwtTokenProvider.validate("access-token")).thenReturn(true);
        when(jwtTokenProvider.getUserId("access-token")).thenReturn(userId);
        when(jwtTokenProvider.getRole("access-token")).thenReturn("ROLE_USER");
        when(userRepository.existsByUserIdAndDeletedAtIsNull(userId)).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
