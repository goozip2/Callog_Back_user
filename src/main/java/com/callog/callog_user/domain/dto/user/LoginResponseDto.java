package com.callog.callog_user.domain.dto.user;

import com.callog.callog_user.domain.dto.token.TokenDto;
import com.callog.callog_user.remote.userstatus.dto.UserProfileResponse;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class LoginResponseDto {
    private TokenDto.AccessRefreshToken tokens;
    private UserProfileResponse userProfile;

    public TokenDto.AccessRefreshToken getTokens() {
        return tokens;
    }

    public UserProfileResponse getUserProfile() {
        return userProfile;
    }
}
