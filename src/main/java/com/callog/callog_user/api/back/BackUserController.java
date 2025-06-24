package com.callog.callog_user.api.back;

import com.callog.callog_user.domain.dto.user.UserInfoDto;
import com.callog.callog_user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BackUserController {
    private final UserService userService;

    @GetMapping("/user/info")
    public UserInfoDto.Response getUserInfo(@RequestParam Long userId) {
        log.info("[Diet 서비스]: email 전송 위해 user 정보 조회 완료");
        UserInfoDto.Response response = userService.getUserInfo(userId);
        return response;
    }
}
