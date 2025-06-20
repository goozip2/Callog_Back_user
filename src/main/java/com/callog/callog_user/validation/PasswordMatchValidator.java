package com.callog.callog_user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    private String passwordFieldName;
    private String passwordConfirmFieldName;

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        // 어노테이션에서 설정한 필드명들을 가져와
        this.passwordFieldName = constraintAnnotation.password();
        this.passwordConfirmFieldName = constraintAnnotation.passwordConfirm();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            // 🔍 리플렉션을 사용해서 필드 값을 가져와
            // 리플렉션 = 런타임에 클래스 정보를 조회하고 조작하는 기능

            // 1️⃣ password 필드 값 가져오기
            Field passwordField = value.getClass().getDeclaredField(passwordFieldName);
            passwordField.setAccessible(true);  // private 필드에 접근 가능하게 설정
            Object passwordValue = passwordField.get(value);

            // 2️⃣ passwordConfirm 필드 값 가져오기
            Field passwordConfirmField = value.getClass().getDeclaredField(passwordConfirmFieldName);
            passwordConfirmField.setAccessible(true);
            Object passwordConfirmValue = passwordConfirmField.get(value);

            // 3️⃣ 둘 다 null이면 통과 (다른 @NotBlank에서 처리하니까)
            if (passwordValue == null && passwordConfirmValue == null) {
                return true;
            }

            // 4️⃣ 하나만 null이면 실패
            if (passwordValue == null || passwordConfirmValue == null) {
                return false;
            }

            // 5️⃣ 문자열 값이 같은지 비교
            boolean isMatched = passwordValue.toString().equals(passwordConfirmValue.toString());

            log.debug("비밀번호 일치 검증: {}", isMatched);
            return isMatched;

        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 필드를 찾을 수 없거나 접근할 수 없으면 검증 실패
            log.error("비밀번호 검증 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }
}