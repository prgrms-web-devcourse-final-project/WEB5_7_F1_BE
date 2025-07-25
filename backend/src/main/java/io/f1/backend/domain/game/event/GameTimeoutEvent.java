package io.f1.backend.domain.game.event;

import io.f1.backend.domain.game.model.Room;

public record GameTimeoutEvent(Room room) {

}
