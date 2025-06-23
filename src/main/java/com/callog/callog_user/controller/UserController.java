package com.callog.callog_user.controller;

import com.callog.callog_user.common.dto.ApiResponseDto;
import com.callog.callog_user.config.jwt.JwtUtil;
import com.callog.callog_user.dto.UserLoginDto;
import com.callog.callog_user.dto.UserRegisterDto;
import com.callog.callog_user.dto.UserUpdateDto;
import com.callog.callog_user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

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

    @PostMapping("/logout")
    public ApiResponseDto<String> logout(
            Authentication authentication,
            HttpServletRequest request) {

        String currentUserId = authentication.getName();

        //🔥 Authorization 헤더에서 토큰 추출
        String authHeader = request.getHeader("Authorization");
        String token = jwtUtil.resolveToken(authHeader); // "Bearer " 제거하고 순수 토큰만 추출
        userService.logout(currentUserId,token);
        return ApiResponseDto.createOk("로그아웃이 완료되었습니다!");
    }
}
