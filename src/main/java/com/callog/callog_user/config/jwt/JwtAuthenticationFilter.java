package com.callog.callog_user.config.jwt;

import com.callog.callog_user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1️⃣ Authorization 헤더 추출
        String authHeader = request.getHeader("Authorization");
        log.debug("Authorization 헤더: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Authorization 헤더가 없거나 Bearer 토큰이 아닙니다.");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2️⃣ 토큰 추출 및 검증
            String token = jwtUtil.resolveToken(authHeader);
            log.debug("추출된 토큰: {}", token);

            if (token != null) {
                if (userService.isTokenBlacklisted(token)) {
                    log.warn("블랙리스트에 등록된 토큰입니다. 접근을 거부합니다.");
                    filterChain.doFilter(request, response);
                    return;
                }

                if (jwtUtil.validateToken(token)) {
                    // 3️⃣ 사용자 정보 추출
                    String userId = jwtUtil.getUserIdFromToken(token);
                    log.debug("토큰에서 추출된 사용자 ID: {}", userId);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                    // 생성자 파라미터 설명:
                    // - principal: 인증된 사용자 (우리는 userId 문자열 사용)
                    // - credentials: 인증 정보 (JWT에서는 null, 이미 검증했으니까)
                    // - authorities: 사용자 권한 (지금은 빈 리스트, 나중에 권한 기능 추가할 때 수정)

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("사용자 {}의 인증이 완료되었습니다.", userId);
                } else {
                    log.warn("유효하지 않은 토큰입니다.");
                }
            }
            }catch(Exception e){
                log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
            }
            filterChain.doFilter(request, response);
        }

        @Override
        protected boolean shouldNotFilter (HttpServletRequest request){
            String path = request.getRequestURI();

            return path.startsWith("/user/login") ||
                    path.startsWith("/user/register") ||
                    path.startsWith("/h2-console") ||
                    path.startsWith("/error");

        }
    }





