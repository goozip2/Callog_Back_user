package com.callog.callog_user.controller;

import com.callog.callog_user.common.dto.ApiResponseDto;
import com.callog.callog_user.dto.UserLoginDto;
import com.callog.callog_user.dto.UserRegisterDto;
import com.callog.callog_user.dto.UserUpdateDto;
import com.callog.callog_user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponseDto<String> register(@RequestBody @Valid UserRegisterDto dto) {
        userService.register(dto);
        return ApiResponseDto.createOk("회원가입이 완료되었습니다!");
    }

    @PostMapping("/login")
    public ApiResponseDto<String> login(@RequestBody @Valid UserLoginDto dto) {
        String jwtToken = userService.login(dto);
        return ApiResponseDto.createOk(jwtToken);
    }

    @PostMapping("/update")
    public ApiResponseDto<String> updateUser(
            @RequestBody @Valid UserUpdateDto dto,
            Authentication authentication) {
        String currentUserId = authentication.getName();
        userService.updateUser(currentUserId, dto);
        return ApiResponseDto.createOk("신체정보가 수정되었습니다!");
    }
}
