package com.callog.callog_user.config.jwt;

import com.callog.callog_user.dto.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenGenerator {
    private final JwtConfigProperties configProperties;
    private volatile SecretKey secretKey; // volatile: 멀티스레드 환경에서 안전하게!

    // 🔐 비밀키 생성 (싱글톤 패턴으로 한 번만 생성)
    private SecretKey getSecretKey() {
        if (secretKey == null) {
            synchronized (this) { // 동기화로 스레드 안전성 보장
                if (secretKey == null) {
                    String configSecret = configProperties.getSecretKey();
                    String hardcodedSecret = "localDevelopmentSecretKeyForTestingOnly123456789";

                    // null이거나 빈 문자열이면 하드코딩 사용
                    String finalSecret = (configSecret != null && !configSecret.trim().isEmpty())
                            ? configSecret
                            : hardcodedSecret;

                    secretKey = Keys.hmacShaKeyFor(finalSecret.getBytes());

                    // 디버깅 로그
                    log.warn("🔐 JWT Secret 사용: {}",
                            configSecret != null ? "설정파일 로드됨" : "하드코딩 fallback");
                    log.info("시크릿 키 길이: {}바이트", finalSecret.length());
                }
            }
        }
        return secretKey;
    }

    // 🎯 액세스 토큰만 생성하는 메서드 (토큰 갱신할 때 사용)
    public TokenDto.AccessToken generateAccessToken(String username, String deviceType) {
        TokenDto.JwtToken jwtToken = this.generateJwtToken(username, deviceType, false);
        return new TokenDto.AccessToken(jwtToken);
    }

    // 🎯 액세스 토큰 + 리프레시 토큰 둘 다 생성하는 메서드 (로그인할 때 사용)
    public TokenDto.AccessRefreshToken generateAccessRefreshToken(String username, String deviceType) {
        TokenDto.JwtToken accessJwtToken = this.generateJwtToken(username, deviceType, false);   // 액세스 토큰
        TokenDto.JwtToken refreshJwtToken = this.generateJwtToken(username, deviceType, true);   // 리프레시 토큰
        return new TokenDto.AccessRefreshToken(accessJwtToken, refreshJwtToken);
    }

    // 🚪 로그아웃용 메서드 - 즉시 만료되는 토큰 생성
    public TokenDto.LogoutToken generateLogoutToken(String username, String deviceType) {
        // 현재 시간을 만료시간으로 설정 (즉시 만료)
        Date now = new Date();
        Date expiredTime = new Date(now.getTime() - 1000); // 1초 전으로 설정 (완전 만료)

        String expiredAccessToken = Jwts.builder()
                .issuer("callog")
                .subject(username)
                .claim("username", username)
                .claim("deviceType", deviceType)
                .claim("tokenType", "access")
                .claim("loggedOut", true) // 로그아웃 토큰임을 명시
                .issuedAt(now)
                .expiration(expiredTime) // 이미 만료된 시간으로 설정
                .signWith(getSecretKey())
                .header().add("typ", "JWT")
                .and()
                .compact();

        String expiredRefreshToken = Jwts.builder()
                .issuer("callog")
                .subject(username)
                .claim("username", username)
                .claim("deviceType", deviceType)
                .claim("tokenType", "refresh")
                .claim("loggedOut", true) // 로그아웃 토큰임을 명시
                .issuedAt(now)
                .expiration(expiredTime) // 이미 만료된 시간으로 설정
                .signWith(getSecretKey())
                .header().add("typ", "JWT")
                .and()
                .compact();

        log.debug("로그아웃 토큰 생성 완료: username={}, 만료시간={}", username, expiredTime);

        return new TokenDto.LogoutToken(
                new TokenDto.JwtToken(expiredAccessToken, 0),
                new TokenDto.JwtToken(expiredRefreshToken, 0)
        );
    }

    // 🔧 실제 JWT 토큰을 생성하는 핵심 메서드
    public TokenDto.JwtToken generateJwtToken(String username, String deviceType, boolean refreshToken) {
        int tokenExpiresIn = tokenExpiresIn(refreshToken, deviceType);
        String tokenType = refreshToken ? "refresh" : "access";

        String token = Jwts.builder()
                .issuer("callog")                        // 토큰 발행자
                .subject(username)                         // 토큰 주체 (사용자 ID)
                .claim("username", username)                 // 사용자 ID 정보
                .claim("deviceType", deviceType)         // 디바이스 타입 (WEB, MOBILE 등)
                .claim("tokenType", tokenType)           // 토큰 타입 (access/refresh)
                .issuedAt(new Date())                    // 토큰 발행 시간
                .expiration(new Date(System.currentTimeMillis() + tokenExpiresIn * 1000L)) // 만료 시간
                .signWith(getSecretKey())                // 서명
                .header().add("typ", "JWT")              // 헤더에 타입 추가
                .and()
                .compact();

        log.debug("{}토큰 생성 완료: username={}, deviceType={}, expiresIn={}초",
                tokenType, username, deviceType, tokenExpiresIn);

        return new TokenDto.JwtToken(token, tokenExpiresIn);
    }

    // ⏰ 토큰 만료시간 계산하는 메서드
    private int tokenExpiresIn(boolean refreshToken, String deviceType) {
        int expiresIn = 60 * 15; // 기본값: 15분

        if (refreshToken) {
            try {
                // 리프레시 토큰인 경우 디바이스 타입에 따라 만료시간 설정
                if (deviceType != null) {
                    if (deviceType.equals("WEB")) {
                        Integer configValue = configProperties.getExpiresIn();
                        expiresIn = (configValue != null) ? configValue : 86400; // 1일 기본값
                    } else if (deviceType.equals("MOBILE")) {
                        Integer configValue = configProperties.getMobileExpiresIn();
                        expiresIn = (configValue != null) ? configValue : 31536000; // 1년 기본값
                    } else if (deviceType.equals("TABLET")) {
                        Integer configValue = configProperties.getTabletExpiresIn();
                        expiresIn = (configValue != null) ? configValue : 31536000; // 1년 기본값
                    }
                } else {
                    Integer configValue = configProperties.getExpiresIn();
                    expiresIn = (configValue != null) ? configValue : 86400; // 1일 기본값
                }
            } catch (Exception e) {
                log.warn("설정 읽기 실패, 기본값 사용: {}", e.getMessage());
                expiresIn = 86400; // 1일 기본값
            }
        }
        // 액세스 토큰은 항상 15분으로 고정
        log.debug("토큰 만료시간: {}초 ({})", expiresIn, refreshToken ? "refresh" : "access");

        return expiresIn;
    }

    // ✅ 리프레시 토큰 검증하는 메서드
    public String validateJwtToken(String refreshToken) {
        String username = null;
        final Claims claims = this.verifyAndGetClaims(refreshToken);

        if (claims == null) {
            log.warn("토큰 파싱 실패");
            return null;
        }

        // 만료시간 체크
        Date expirationDate = claims.getExpiration();
        if (expirationDate == null || expirationDate.before(new Date())) {
            log.warn("만료된 토큰");
            return null;
        }

        username = claims.get("username", String.class);
        String tokenType = claims.get("tokenType", String.class);

        // 리프레시 토큰인지 확인
        if (!"refresh".equals(tokenType)) {
            log.warn("리프레시 토큰이 아닙니다. tokenType: {}", tokenType);
            return null;
        }

        // 로그아웃된 토큰인지 확인
        Boolean loggedOut = claims.get("loggedOut", Boolean.class);
        if (Boolean.TRUE.equals(loggedOut)) {
            log.warn("로그아웃된 리프레시 토큰입니다.");
            return null;
        }

        log.debug("리프레시 토큰 검증 성공: username={}", username);
        return username;
    }

    // 🔍 토큰을 파싱하고 Claims를 추출하는 메서드
    private Claims verifyAndGetClaims(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.warn("토큰 검증 실패: {}", e.getMessage());
            claims = null;
        }
        return claims;
    }
}