package com.callog.callog_user.remote.userstatus.dto;

import com.callog.callog_user.remote.userstatus.Gender;
import lombok.*;


@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class UserProfileResponse {
    private Long height;
    private Long weight;
    private Long age;
    private Gender gender;
}
