package com.callog.callog_user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDto {

    @NotBlank(message = "닉네임을 입력하세요.")
    private String userName;

    //비어있으면 기존 값 유지
    private String password;

    //새로운 비밀번호가 있는지 확인
    public boolean hasNewPassword() {
        return password != null && !password.trim().isEmpty();
    }
}