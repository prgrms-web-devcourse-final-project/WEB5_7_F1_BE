package io.f1.backend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record SignupRequestDto(@NotBlank(message = "닉네임을 입력하세요") String nickname) {}
