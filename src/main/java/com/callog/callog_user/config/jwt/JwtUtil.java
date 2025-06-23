package com.callog.callog_user.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    // 🔐 비밀키 생성 (TokenGenerator와 동일한 방식)
    private SecretKey getSecretKey() {
        if (key == null) {
            synchronized (this) {
                if (key == null) {
                    key = Keys.hmacShaKeyFor(configProperties.getSecretKey().getBytes());
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
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    // ✅ 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 액세스 토큰인지 확인 (리프레시 토큰은 인증에 사용하면 안 됨)
            String tokenType = claims.get("tokenType", String.class);
            if (!"access".equals(tokenType)) {
                log.warn("액세스 토큰이 아닙니다. tokenType: {}", tokenType);
                return false;
            }

            return true;

        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰입니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 토큰입니다: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 토큰입니다: {}", e.getMessage());
        } catch (SecurityException | IllegalArgumentException e) {
            log.warn("유효하지 않은 토큰입니다: {}", e.getMessage());
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