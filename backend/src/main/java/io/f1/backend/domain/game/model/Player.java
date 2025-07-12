package io.f1.backend.domain.game.model;

import lombok.Getter;

@Getter
public class Player {

    public final Long id;

    public final String nickname;

    private boolean isReady = false;

    private ConnectionState state = ConnectionState.CONNECTED;

    private int correctCount = 0;

    public Player(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }
}
