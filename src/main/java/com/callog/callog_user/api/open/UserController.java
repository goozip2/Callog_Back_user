package com.callog.callog_user.api.open;

import com.callog.callog_user.common.dto.ApiResponseDto;
import com.callog.callog_user.config.jwt.JwtUtil;
import com.callog.callog_user.domain.dto.*;
import com.callog.callog_user.repository.UserRepository;
import com.callog.callog_user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ApiResponseDto<String> register(@RequestBody @Valid UserRegisterDto dto) {
        userService.register(dto);
        return ApiResponseDto.createOk("회원가입이 완료되었습니다!");
    }

    @PostMapping("/login")
    public ApiResponseDto<TokenDto.AccessRefreshToken> login(@RequestBody @Valid UserLoginDto dto) {
        TokenDto.AccessRefreshToken tokens = userService.login(dto);
        return ApiResponseDto.createOk(tokens);
    }

    @PostMapping("/refresh")
    public ApiResponseDto<TokenDto.AccessToken> refresh(@RequestBody @Valid UserRefreshDto refreshDto) {
        TokenDto.AccessToken newAccessToken = userService.refresh(refreshDto.getToken());
        return ApiResponseDto.createOk(newAccessToken);
    }

    @PostMapping("/logout")
    public ApiResponseDto<TokenDto.LogoutToken> logout(Authentication authentication) {
        String currentUserId = authentication.getName();
        TokenDto.LogoutToken logoutTokens = userService.logout(currentUserId);
        return ApiResponseDto.createOk(logoutTokens);
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
