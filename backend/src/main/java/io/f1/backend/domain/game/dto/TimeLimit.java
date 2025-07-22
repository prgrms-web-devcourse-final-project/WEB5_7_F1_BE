package io.f1.backend.domain.game.dto;

import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.GameErrorCode;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TimeLimit {
    FIFTEEN(15),
    THIRTY(30),
    FORTY_FIVE(45),
    SIXTY(60);

    private final int value;

    TimeLimit(int value) {
        this.value = value;
    }

    public static TimeLimit from(int value) {
        return Arrays.stream(values())
                .filter(t -> t.value == value)
                .findFirst()
                .orElseThrow(() -> new CustomException(GameErrorCode.GAME_SETTING_CONFLICT));
    }
}
