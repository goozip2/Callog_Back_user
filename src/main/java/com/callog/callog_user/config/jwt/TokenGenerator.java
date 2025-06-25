package com.callog.callog_user.config.jwt;

import com.callog.callog_user.domain.dto.token.TokenDto;
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
    private volatile SecretKey secretKey;

    // 🔐 비밀키 생성 (로그 제거)
    private SecretKey getSecretKey() {
        if (secretKey == null) {
            // 초기화 필요한 경우, 동기화 블록 진입
            // Monitor Lock을 걸면, 한 번에 하나의 스레드만 진입 가능 -> 다른 스레드는 락을 획득할 때까지 대기 상태(BLOCKED)로 들어감
            // 서로 간섭 없이 임계 영역이 실행되도록 보장
            // 다시 확인 (double check: 여러 스레드가 동기화 블록에 진입했더라도 최초 1개 스레드만 초기화하도록 보장)
            synchronized (this) {
                if (secretKey == null) {
//                    secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(configProperties.getSecretKey()));
                    secretKey = Keys.hmacShaKeyFor(configProperties.getSecretKey().getBytes());
                }
            }
        }
        log.info("시크릿키!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!{}",secretKey);
        return secretKey;
    }

    public TokenDto.AccessToken generateAccessToken(String username,Long userId, String deviceType) {
        TokenDto.JwtToken jwtToken = this.generateJwtToken(username, userId,deviceType, false);
        return new TokenDto.AccessToken(jwtToken);
    }

    public TokenDto.AccessRefreshToken generateAccessRefreshToken(String username,Long userId, String deviceType) {
        TokenDto.JwtToken accessJwtToken = this.generateJwtToken(username, userId,deviceType, false);
        TokenDto.JwtToken refreshJwtToken = this.generateJwtToken(username,userId, deviceType, true);
        return new TokenDto.AccessRefreshToken(accessJwtToken, refreshJwtToken);
    }

    public TokenDto.LogoutToken generateLogoutToken(String username, Long userId,String deviceType) {
        Date now = new Date();
        Date expiredTime = new Date(now.getTime() - 1000); // 1초 전으로 설정

        String expiredAccessToken = Jwts.builder()
                .issuer("callog")
                .subject(username)
                .claim("userId",userId)
                .claim("username", username)
                .claim("deviceType", deviceType)
                .claim("tokenType", "access")
                .claim("loggedOut", true)
                .issuedAt(now)
                .expiration(expiredTime)
                .signWith(getSecretKey())
                .header().add("typ", "JWT")
                .and()
                .compact();

        String expiredRefreshToken = Jwts.builder()
                .issuer("callog")
                .subject(username)
                .claim("userId",userId)
                .claim("username", username)
                .claim("deviceType", deviceType)
                .claim("tokenType", "refresh")
                .claim("loggedOut", true)
                .issuedAt(now)
                .expiration(expiredTime)
                .signWith(getSecretKey())
                .header().add("typ", "JWT")
                .and()
                .compact();

        return new TokenDto.LogoutToken(
                new TokenDto.JwtToken(expiredAccessToken, 0),
                new TokenDto.JwtToken(expiredRefreshToken, 0)
        );
    }

    public TokenDto.JwtToken generateJwtToken(String username,Long userId, String deviceType, boolean refreshToken) {
        int tokenExpiresIn = tokenExpiresIn(refreshToken, deviceType);
        String tokenType = refreshToken ? "refresh" : "access";

        String token = Jwts.builder()
                .issuer("callog")
                .subject(username)
                .claim("username", username)
                .claim("userId",userId)
                .claim("deviceType", deviceType)
                .claim("tokenType", tokenType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpiresIn * 1000L))
                .signWith(getSecretKey())
                .header().add("typ", "JWT")
                .and()
                .compact();

        return new TokenDto.JwtToken(token, tokenExpiresIn);
    }

    private int tokenExpiresIn(boolean refreshToken, String deviceType) {
        int expiresIn = 60 * 60; // 기본값: 15분 ▶ 1시간

        if (refreshToken) {
            try {
                if (deviceType != null) {
                    if (deviceType.equals("WEB")) {
                        Integer configValue = configProperties.getExpiresIn();
                        expiresIn = (configValue != null) ? configValue : 86400;
                    } else if (deviceType.equals("MOBILE")) {
                        Integer configValue = configProperties.getMobileExpiresIn();
                        expiresIn = (configValue != null) ? configValue : 31536000;
                    } else if (deviceType.equals("TABLET")) {
                        Integer configValue = configProperties.getTabletExpiresIn();
                        expiresIn = (configValue != null) ? configValue : 31536000;
                    }
                } else {
                    Integer configValue = configProperties.getExpiresIn();
                    expiresIn = (configValue != null) ? configValue : 86400;
                }
            } catch (Exception e) {
                log.warn("토큰 설정 읽기 실패, 기본값 사용"); // 📝 간소화
                expiresIn = 86400;
            }
        }

        return expiresIn;
    }

    public String validateJwtToken(String refreshToken) {
        final Claims claims = this.verifyAndGetClaims(refreshToken);

        if (claims == null) {
            return null;
        }

        Date expirationDate = claims.getExpiration();
        if (expirationDate == null || expirationDate.before(new Date())) {
            return null;
        }

        String username = claims.get("username", String.class);
        String tokenType = claims.get("tokenType", String.class);

        if (!"refresh".equals(tokenType)) {
            return null;
        }

        Boolean loggedOut = claims.get("loggedOut", Boolean.class);
        if (Boolean.TRUE.equals(loggedOut)) {
            return null;
        }

        return username;
    }

    private Claims verifyAndGetClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}