package io.f1.backend.domain.user.mapper;

import io.f1.backend.domain.user.dto.SignupResponse;
import io.f1.backend.domain.user.entity.User;

public class UserMapper {

    private UserMapper() {}

    public static SignupResponse toSignupResponse(User user) {
        return new SignupResponse(user.getId(), user.getNickname());
    }
}
