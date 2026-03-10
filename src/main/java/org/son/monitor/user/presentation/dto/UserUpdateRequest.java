package org.son.monitor.user.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(

        @NotBlank(message = "이름은 필수입니다.")
        String name
) {
}