package com.callog.callog_user.domain.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JwtToken {
        private String token;
        private Integer expiresIn;
    }

    @Getter
    @RequiredArgsConstructor
    public static class AccessToken {
        private final JwtToken access;
    }

    @Getter
    @RequiredArgsConstructor
    public static class AccessRefreshToken {
        private final JwtToken access;
        private final JwtToken refresh;
    }

    @Getter
    @RequiredArgsConstructor
    public static class LogoutToken {
        private final JwtToken access;
        private final JwtToken refresh;

        private final boolean loggedOut = true;
        private final String message = "로그아웃이 완료되었습니다.";
    }
}