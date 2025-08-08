package io.f1.backend.domain.game.event;

import io.f1.backend.domain.game.model.Room;
import io.f1.backend.domain.quiz.entity.Quiz;

public record RoomCreatedEvent(Room room, Quiz quiz, long questionSize) {}
