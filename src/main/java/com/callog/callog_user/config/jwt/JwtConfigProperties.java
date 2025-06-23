package com.callog.callog_user.config.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(value = "jwt", ignoreUnknownFields = true)
@Getter
@Setter
public class JwtConfigProperties {
    private Integer expiresIn;        // 액세스 토큰 만료시간
    private Integer mobileExpiresIn;  // 모바일 리프레시 토큰 만료시간
    private Integer tabletExpiresIn;  // 태블릿 리프레시 토큰 만료시간
    private String secretKey;         // JWT 서명용 비밀키
}