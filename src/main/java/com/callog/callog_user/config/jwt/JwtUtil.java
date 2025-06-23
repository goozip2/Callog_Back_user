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
    private volatile SecretKey key;

    //  비밀키 생성
    private SecretKey getSecretKey() {
        if (key == null) {
            synchronized (this) {
                if (key == null) {
                    String configSecret = configProperties.getSecretKey();
                    String hardcodedSecret = "localDevelopmentSecretKeyForTestingOnly123456789";

                    String finalSecret = (configSecret != null && !configSecret.trim().isEmpty())
                            ? configSecret
                            : hardcodedSecret;

                    key = Keys.hmacShaKeyFor(finalSecret.getBytes());
                }
            }
        }
        return key;
    }

    // 토큰 생성
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 15 * 60 * 1000); // 15분

        return Jwts.builder()
                .subject(username)
                .claim("username", username)
                .claim("tokenType", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey())
                .compact();
    }

    //  토큰에서 사용자 ID 추출
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 액세스 토큰인지 확인
            String tokenType = claims.get("tokenType", String.class);
            if (!"access".equals(tokenType)) {
                return false;
            }

            // 로그아웃 토큰인지 확인
            Boolean loggedOut = claims.get("loggedOut", Boolean.class);
            if (Boolean.TRUE.equals(loggedOut)) {
                return false;
            }

            // 만료시간 체크
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                return false;
            }

            // 사용자명 확인
            String username = claims.get("username", String.class);
            if (username == null || username.trim().isEmpty()) {
                return false;
            }

            return true;

        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰 접근 시도");
        } catch (Exception e) {
            log.warn("토큰 검증 실패: {}", e.getClass().getSimpleName());
        }
        return false;
    }

    // Bearer 토큰 처리
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}