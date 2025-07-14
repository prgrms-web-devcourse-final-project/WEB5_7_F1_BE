package io.f1.backend.domain.user.dto;

import io.f1.backend.domain.user.entity.User;

import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
public class SessionUser implements Serializable {

    private final Long userId;
    private final String nickname;
    private final String providerId;
    private final LocalDateTime lastLogin;

    public SessionUser(User user) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.providerId = user.getProviderId();
        this.lastLogin = user.getLastLogin();
    }
}
