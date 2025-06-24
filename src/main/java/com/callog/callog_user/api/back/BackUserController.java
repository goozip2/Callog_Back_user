package com.callog.callog_user.api.back;

import com.callog.callog_user.domain.dto.user.UserInfoDto;
import com.callog.callog_user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BackUserController {
    private final UserService userService;

    @GetMapping("/user/info")
    public UserInfoDto.Response getUserInfo(@RequestParam Long userId) {
        UserInfoDto.Response response = userService.getUserInfo(userId);
        return response;
    }
}
