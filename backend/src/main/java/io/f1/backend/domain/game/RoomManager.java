package io.f1.backend.domain.game;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
	private final Map<Long, Room> rooms = new ConcurrentHashMap<>();
}
