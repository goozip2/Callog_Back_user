package com.callog.callog_user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

// 🎯 비밀번호 일치 여부를 체크하는 커스텀 어노테이션
@Documented
@Constraint(validatedBy = PasswordMatchValidator.class)  // 실제 검증 로직을 담은 클래스
@Target({ElementType.TYPE})  // 클래스 레벨에서 사용 (필드 두 개를 비교해야 하니까)
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatch {

    // 검증 실패 시 표시할 메시지
    String message() default "비밀번호가 일치하지 않습니다.";

    // 검증 그룹 (지금은 사용 안 함)
    Class<?>[] groups() default {};

    // 추가 정보 (지금은 사용 안 함)
    Class<? extends Payload>[] payload() default {};

    // 비밀번호 필드명 (기본값: "password")
    String password() default "password";

    // 비밀번호 확인 필드명 (기본값: "passwordConfirm")
    String passwordConfirm() default "passwordConfirm";
}