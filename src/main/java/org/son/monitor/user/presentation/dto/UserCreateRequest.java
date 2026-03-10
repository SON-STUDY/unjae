package org.son.monitor.user.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.son.monitor.user.domain.User;

public record UserCreateRequest(

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @NotBlank(message = "이름은 필수입니다.")
        String name
) {
    public User toEntity() {
        return User.builder()
                .email(email)
                .name(name)
                .build();
    }
}