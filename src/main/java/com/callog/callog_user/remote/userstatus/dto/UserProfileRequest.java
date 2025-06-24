package com.callog.callog_user.remote.userstatus.dto;

import com.callog.callog_user.remote.userstatus.Gender;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class UserProfileRequest {

    @NotNull @Positive(message = "height must be > 0")
    private Long height;

    @NotNull @Positive(message = "weight must be > 0")
    private Long weight;

    @NotNull @Positive(message = "age must be > 0")
    private Long age;

    @NotNull
    private Gender gender;
}
