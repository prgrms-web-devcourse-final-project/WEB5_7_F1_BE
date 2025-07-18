package io.f1.backend.domain.admin.dto;

import java.time.LocalDateTime;

public record UserResponse(Long id, String nickname, LocalDateTime lastLogin,
                           LocalDateTime createdAt) {

}
