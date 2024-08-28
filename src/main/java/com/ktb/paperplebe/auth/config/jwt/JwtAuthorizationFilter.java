package com.ktb.paperplebe.auth.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktb.paperplebe.auth.dto.JwtClaimResponse;
import com.ktb.paperplebe.auth.service.TokenService;
import com.ktb.paperplebe.common.dto.ExceptionResponse;
import com.ktb.paperplebe.user.constant.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.ktb.paperplebe.auth.config.jwt.JwtUtil.ACCESS_TOKEN;
import static com.ktb.paperplebe.auth.config.jwt.JwtUtil.REFRESH_TOKEN;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        // h2-console 요청은 토큰 검사를 수행하지 않음
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = extractCookie(cookies, ACCESS_TOKEN);
        String refreshToken = extractCookie(cookies, REFRESH_TOKEN);

        // accessToken이 유효한 경우
        if (accessToken != null && jwtUtil.validateToken(accessToken)) {
            setAuthInSecurityContext(accessToken);
            filterChain.doFilter(request, response);
            return;
        }

        // accessToken이 유효하지 않고 refreshToken이 유효하면 accessToken 갱신
        if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
            accessToken = tokenService.renewToken(response, accessToken, refreshToken);
            setAuthInSecurityContext(accessToken);
            filterChain.doFilter(request, response);
            return;
        }

        // accessToken과 refreshToken이 모두 유효하지 않으면 에러 응답 반환
        issueErrorResponse(response);
    }

    private String extractCookie(Cookie[] cookies, String cookieName) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null); // 쿠키가 존재하지 않을 경우 null 반환
    }

    private void issueErrorResponse(HttpServletResponse response) throws IOException {
        ExceptionResponse exceptionResponse = new ExceptionResponse("토큰이 유효하지 않습니다.", 403);
        response.setStatus(403);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));
    }

    private void setAuthInSecurityContext(String accessToken) {
        JwtClaimResponse claimDTO = jwtUtil.extractClaims(accessToken);

        Long userId = claimDTO.getUserId();
        UserRole role = claimDTO.getRole();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userId, null, List.of(role::name));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}