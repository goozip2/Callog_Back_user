package com.callog.callog_user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRefreshDto {

    @NotBlank(message = "리프레시 토큰을 입력하세요.")
    private String token; // 리프레시 토큰
}