package com.callog.callog_user.controller;

import com.callog.callog_user.common.dto.ApiResponseDto;
import com.callog.callog_user.dto.UserLoginDto;
import com.callog.callog_user.dto.UserRegisterDto;
import com.callog.callog_user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponseDto<String> register(@RequestBody @Valid UserRegisterDto dto) {
        userService.register(dto);
        return ApiResponseDto.defaultOk();
    }

    @PostMapping("/login")
    public ApiResponseDto<String> login(@RequestBody @Valid UserLoginDto dto) {
        userService.login(dto);
        return ApiResponseDto.createOk("로그인 성공 ");
    }
}
