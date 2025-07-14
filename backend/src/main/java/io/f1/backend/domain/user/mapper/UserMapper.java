package io.f1.backend.domain.user.mapper;

import io.f1.backend.domain.user.dto.SignupResponseDto;
import io.f1.backend.domain.user.entity.User;

public class UserMapper {

    private UserMapper() {}

    public static SignupResponseDto toSignupResponse(User user) {
        return new SignupResponseDto(user.getId(), user.getNickname());
    }
}
