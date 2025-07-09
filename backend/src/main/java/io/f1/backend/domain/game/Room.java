package io.f1.backend.domain.game;

import io.f1.backend.domain.question.entity.Question;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Room {

	private final Long id;

	private final RoomSetting roomSetting;
	private GameSetting gameSetting;
	private RoomState state = RoomState.WAITING;

	private Player host;

	private List<Question> questions;

	private Map<String, Player> playerSessionMap;

	private final LocalDateTime createdAt = LocalDateTime.now();

}
