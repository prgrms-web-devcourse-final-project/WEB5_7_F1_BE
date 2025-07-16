package io.f1.backend.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminLoginFailResponseDto {
    private String code;
    private String message;
}
