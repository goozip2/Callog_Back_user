package com.callog.callog_user.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j  // 로그를 찍기 위한 어노테이션. log.info(), log.warn() 등을 사용할 수 있게 해줘
@Component  // Spring이 이 클래스를 빈으로 관리하도록 하는 어노테이션
public class JwtUtil {
    private final SecretKey key; //Jwt 서명/검증
    private final long expiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    //Jwt 토큰 생성
    public String generateToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration); //만료 시간 계산

        return Jwts.builder()
                .subject(userId) //userId에 대한 토큰
                .issuedAt(now) // 토큰 발행시간
                .expiration(expiryDate) // 서버에서 자동으로 만료 검증
                .signWith(key) //토큰 보장
                .compact();
    }

    //토큰 정보 꺼내기(토큰 파싱)
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject(); // JWT의 subject 필드에서 우리가 저장한 userId 반환
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰입니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 토큰입니다: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            //JWT 형식이 깨진 경우 (점이 2개가 아니라든지)
            log.warn("잘못된 형식의 토큰입니다: {}", e.getMessage());
        } catch (SecurityException | IllegalArgumentException e) {
            //서명이 틀리거나 키가 잘못된 경우
            log.warn("유효하지 않은 토큰입니다: {}", e.getMessage());
        }
        return false;
    }

    //BearToken처리
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " = 7글자니까 7번째부터 잘라내기
        }
        return null;
    }
}

