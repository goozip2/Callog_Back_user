package com.callog.callog_user.remote.userstatus;

import com.callog.callog_user.remote.userstatus.dto.UserProfileRequest;
import com.callog.callog_user.remote.userstatus.dto.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="callog-user-status", path="/backend/userStatus")
public interface RemoteUserStatusService {
    // 로그인 시 사용하는 backend API (userstat쪽 data를 user로 받아오기)
    @GetMapping("/login/{userId}")
    public UserProfileResponse getUserStats(@PathVariable Long userId);

    // 회원가입 시 사용하는 backend API (user쪽 data를 userstat으로 전달)
    @PostMapping("/register")
    public UserProfileResponse upsertProfile(
            //@RequestHeader("X-USER-ID") String userId,
            @RequestParam Long userId,
            @Valid @RequestBody UserProfileRequest req);
}
