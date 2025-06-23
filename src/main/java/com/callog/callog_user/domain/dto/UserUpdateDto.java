package com.callog.callog_user.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDto {

    @NotNull(message = "키를 입력하세요.")
    @Min(value = 0, message = "키는 0cm 이상이어야 합니다.")
    @Max(value = 200, message = "키는 200cm 이하여야 합니다.")
    private Integer height;

    @NotNull(message = "몸무게를 입력하세요.")
    @Min(value = 0, message = "몸무게는 0kg 이상이어야 합니다.")
    @Max(value = 200, message = "몸무게는 200kg 이하여야 합니다.")
    private Integer weight;

    public boolean hasHeight() {
        return height != null && height > 0; //0보다 큰 값이 있을 때
    }

    public boolean hasWeight(){
        return weight != null && weight > 0;
    }

    public boolean hasAnyUpdate() {
        return hasHeight() || hasWeight();
    }

    public Double getWeightAsDouble() {
        return weight != null ? weight.doubleValue() : null;
    }

}