package com.sogong.todak.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.sogong.todak.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰 추출
        String token = resolveToken(request);

        try {
            // 2. 토큰 유효성 검증 및 인증 처리
            if (StringUtils.hasText(token) && jwtTokenProvider.validate(token)) {
                setAuthenticationToContext(token);
            }
        } catch (ExpiredJwtException e) {
            // 만료된 토큰에 대한 커스텀 처리 (선택 사항: 로그 기록 후 다음 필터로 전달하여 401 유도)
            log.info("JWT token has expired: {}", e.getMessage());
            request.setAttribute("exception", "EXPIRED_TOKEN");
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
            request.setAttribute("exception", "INVALID_TOKEN");
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void setAuthenticationToContext(String token) {
        UUID userId = jwtTokenProvider.getUserId(token);
        String role = jwtTokenProvider.getRole(token);

        if (!userRepository.existsByUserIdAndDeletedAtIsNull(userId)) {
            SecurityContextHolder.clearContext();
            log.info("Skipped authentication for withdrawn user.");
            return;
        }

        // Principal에 UUID를 담되, 필요 시 UserDetails 객체를 생성하여 담을 수 있도록 확장 가능
        var authorities = List.of(new SimpleGrantedAuthority(role));
        var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
