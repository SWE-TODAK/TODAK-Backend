package com.sogong.todak.user.dto.request;

import com.sogong.todak.user.entity.Gender;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateGenderRequest {

    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;
}
