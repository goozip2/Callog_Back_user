package com.callog.callog_user.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenDto {

    // 🎯 JWT 토큰 정보를 담는 기본 클래스
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JwtToken {
        private String token;      // 실제 토큰 문자열
        private Integer expiresIn; // 만료 시간 (초 단위)
    }

    // 🔑 액세스 토큰만 담는 클래스 (토큰 갱신할 때 사용)
    @Getter
    @RequiredArgsConstructor
    public static class AccessToken {
        private final JwtToken access;
    }

    // 🔐 액세스 토큰 + 리프레시 토큰 둘 다 담는 클래스 (로그인할 때 사용)
    @Getter
    @RequiredArgsConstructor
    public static class AccessRefreshToken {
        private final JwtToken access;   // 액세스 토큰
        private final JwtToken refresh;  // 리프레시 토큰
    }

    // 🚪 로그아웃용 토큰 클래스 - 만료된 토큰들을 담음
    @Getter
    @RequiredArgsConstructor
    public static class LogoutToken {
        private final JwtToken access;   // 만료된 액세스 토큰
        private final JwtToken refresh;  // 만료된 리프레시 토큰

        // 클라이언트에게 로그아웃 상태임을 알려주는 플래그
        private final boolean loggedOut = true;
        private final String message = "로그아웃이 완료되었습니다.";
    }
}