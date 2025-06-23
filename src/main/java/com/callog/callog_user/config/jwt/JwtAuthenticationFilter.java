package com.callog.callog_user.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.debug("🔍 JWT 필터 시작: {} {}", method, requestURI);

        // 1️⃣ Authorization 헤더 추출
        String authHeader = request.getHeader("Authorization");
        log.debug("📋 Authorization 헤더: {}", authHeader != null ? "Bearer ****" : "없음");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("❌ Authorization 헤더가 없거나 Bearer 토큰이 아닙니다. URI: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2️⃣ 토큰 추출 및 검증
            String token = jwtUtil.resolveToken(authHeader);
            log.debug("🎯 토큰 추출: {}", token != null ? "성공" : "실패");

            if (token != null) {
                boolean isValid = jwtUtil.validateToken(token);
                log.debug("🔒 토큰 검증: {}", isValid ? "성공" : "실패");

                if (isValid) {
                    // 3️⃣ 사용자 정보 추출
                    String userId = jwtUtil.getUsernameFromToken(token);
                    log.debug("👤 사용자 ID 추출: {}", userId);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("✅ 사용자 {}의 인증 완료", userId);
                } else {
                    log.warn("❌ 토큰 검증 실패. URI: {}", requestURI);
                }
            } else {
                log.warn("❌ 토큰 추출 실패. URI: {}", requestURI);
            }
        } catch (Exception e) {
            log.error("🚨 JWT 토큰 처리 중 오류: {}", e.getMessage());
            e.printStackTrace(); // 스택 트레이스 출력
        }

        // 4️⃣ 최종 인증 상태 확인
        Authentication finalAuth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = finalAuth != null && finalAuth.isAuthenticated();
        log.debug("🔐 최종 인증 상태: {} (URI: {})", isAuthenticated ? "인증됨" : "미인증", requestURI);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        boolean skip = path.startsWith("/user/login") ||
                path.startsWith("/user/register") ||
                path.startsWith("/user/refresh") ||
                path.startsWith("/h2-console") ||
                path.startsWith("/error");

        log.debug("🚥 필터 스킵 여부: {} (경로: {})", skip, path);
        return skip;
    }
}