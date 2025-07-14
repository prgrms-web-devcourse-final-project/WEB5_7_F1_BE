package io.f1.backend.domain.game.dto;

import io.f1.backend.domain.game.dto.response.GameSettingResponse;
import io.f1.backend.domain.game.dto.response.PlayerListResponse;
import io.f1.backend.domain.game.dto.response.RoomSettingResponse;

public record RoomInitialData(
        String destination,
        RoomSettingResponse roomSettingResponse,
        GameSettingResponse gameSettingResponse,
        PlayerListResponse playerListResponse) {}
