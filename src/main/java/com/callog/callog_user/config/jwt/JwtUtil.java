package com.callog.callog_user.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtConfigProperties configProperties;
    private volatile SecretKey key; // TokenGenerator와 동일한 키 사용

    // 🔐 비밀키 생성 (TokenGenerator와 동일한 방식) - NULL-SAFE 처리!
    private SecretKey getSecretKey() {
        if (key == null) {
            synchronized (this) {
                if (key == null) {
                    String configSecret = configProperties.getSecretKey();
                    String hardcodedSecret = "localDevelopmentSecretKeyForTestingOnly123456789";

                    // ✅ Null-safe 처리
                    String finalSecret = (configSecret != null && !configSecret.trim().isEmpty())
                            ? configSecret
                            : hardcodedSecret;

                    key = Keys.hmacShaKeyFor(finalSecret.getBytes());

                    log.warn("🔐 JwtUtil Secret 사용: {}",
                            configSecret != null ? "설정파일 로드됨" : "하드코딩 fallback");
                    log.info("JwtUtil 시크릿 키 길이: {}바이트", finalSecret.length());
                }
            }
        }
        return key;
    }

    // 🎯 기존 토큰 생성 메서드 (하위 호환성을 위해 유지)
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 15 * 60 * 1000); // 15분

        return Jwts.builder()
                .subject(username)
                .claim("username", username)
                .claim("tokenType", "access") // 액세스 토큰으로 명시
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey())
                .compact();
    }

    // 🔍 토큰에서 사용자 ID 추출
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            log.warn("토큰에서 사용자명 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    // ✅ 토큰 유효성 검증 - 로그아웃 토큰 체크 + 상세 로깅
    public boolean validateToken(String token) {
        log.debug("🔍 토큰 검증 시작: {}", token != null ? "토큰 있음" : "토큰 없음");

        if (token == null || token.trim().isEmpty()) {
            log.warn("❌ 토큰이 null이거나 비어있음");
            return false;
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            log.debug("✅ JWT 파싱 성공");

            // 1️⃣ 액세스 토큰인지 확인
            String tokenType = claims.get("tokenType", String.class);
            log.debug("🏷️ 토큰 타입: {}", tokenType);

            if (!"access".equals(tokenType)) {
                log.warn("❌ 액세스 토큰이 아닙니다. tokenType: {}", tokenType);
                return false;
            }

            // 2️⃣ 로그아웃 토큰인지 확인
            Boolean loggedOut = claims.get("loggedOut", Boolean.class);
            if (Boolean.TRUE.equals(loggedOut)) {
                log.warn("❌ 로그아웃된 토큰입니다.");
                return false;
            }

            // 3️⃣ 만료시간 체크
            Date expiration = claims.getExpiration();
            Date now = new Date();
            log.debug("⏰ 토큰 만료시간: {}, 현재시간: {}", expiration, now);

            if (expiration.before(now)) {
                log.warn("❌ 만료된 토큰입니다. 만료: {}, 현재: {}", expiration, now);
                return false;
            }

            // 4️⃣ 사용자명 확인
            String username = claims.get("username", String.class);
            log.debug("👤 토큰 내 사용자명: {}", username);

            if (username == null || username.trim().isEmpty()) {
                log.warn("❌ 토큰에 사용자명이 없습니다.");
                return false;
            }

            log.debug("✅ 토큰 검증 성공: {}", username);
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("❌ 만료된 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("❌ 지원되지 않는 토큰: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("❌ 잘못된 형식의 토큰: {}", e.getMessage());
        } catch (SecurityException | IllegalArgumentException e) {
            log.warn("❌ 유효하지 않은 토큰: {}", e.getMessage());
        } catch (Exception e) {
            log.error("🚨 토큰 검증 중 예상치 못한 오류: {}", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // 🎯 Bearer 토큰 처리
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}