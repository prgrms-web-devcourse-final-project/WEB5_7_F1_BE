package io.f1.backend.domain.stat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatChangeEvent {

    private Long userId;
    private boolean win;
    private int deltaScore;

    public static StatChangeEvent of(Long userId, boolean win, int deltaScore) {
        return StatChangeEvent.builder().userId(userId).win(win).deltaScore(deltaScore).build();
    }
}
