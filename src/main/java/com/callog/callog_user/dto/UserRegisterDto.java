package com.callog.callog_user.dto;

import com.callog.callog_user.entity.User;
import com.callog.callog_user.validation.PasswordMatch;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
//@PasswordMatch
public class UserRegisterDto {

    @NotBlank(message = "아이디를 입력하세요.")
    private String username;

    @NotBlank(message = "비밀번호를 입력하세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력하세요.")
    private String passwordCheck;

    @NotBlank(message = "닉네임을 입력하세요.")
    private String nikename;

    @NotNull(message = "키를 입력하세요.")
    @Min(value = 0, message = "키는 0cm 이상이어야 합니다.")
    @Max(value = 200, message = "키는 200cm 이하여야 합니다.")
    private Integer height;

    @NotNull(message = "출생년도를 입력하세요.")
    @Min(value = 1900, message = "출생년도는 1900년 이상이어야 합니다.")
    @Max(value = 2025, message = "출생년도는 2025년 이하여야 합니다.")
    private Integer age;  // 출생년도만 받기! (예: 1998)

    @NotNull(message = "몸무게를 입력하세요.")
    @Min(value = 0, message = "몸무게는 0kg 이상이어야 합니다.")
    @Max(value = 200, message = "몸무게는 200kg 이하여야 합니다.")
    private Integer weight;

    @NotBlank(message = "성별을 선택하세요.")
    @Pattern(regexp = "^(male|female)$",
            message = "성별은 male 또는 female이어야 합니다.")
    private String gender;

    public User toEntity() {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nikename);
        user.setHeight(height);
        user.setWeight(weight.doubleValue());
        user.setAge(age);
        user.setGender(gender);

        return user;
    }

}
