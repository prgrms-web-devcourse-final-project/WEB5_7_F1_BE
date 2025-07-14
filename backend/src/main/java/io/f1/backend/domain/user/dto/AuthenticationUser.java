package io.f1.backend.domain.user.dto;

import io.f1.backend.domain.user.entity.User;
import java.io.Serializable;

public record AuthenticationUser(Long userId, String nickname, String providerId) implements
    Serializable {

    public static AuthenticationUser from(User user) {
        return new AuthenticationUser(
            user.getId(),
            user.getNickname(),
            user.getProviderId()
        );
    }
}
