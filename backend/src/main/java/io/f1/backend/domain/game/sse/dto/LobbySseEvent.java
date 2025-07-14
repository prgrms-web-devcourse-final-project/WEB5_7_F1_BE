package io.f1.backend.domain.game.sse.dto;

public record LobbySseEvent<T>(String type, T payload) {}
