package com.callog.callog_user.domain.dto.user;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserInfoDto {
    @Getter
    @Setter
    public static class Request {
        private Long userId; // 숫자 형식
    }
    @Getter
    @Setter
    public static class Response {
        private String username; // email 형식
        private String nickname;
    }
}
