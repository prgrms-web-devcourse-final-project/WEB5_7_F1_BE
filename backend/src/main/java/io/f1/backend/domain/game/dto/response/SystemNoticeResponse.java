package io.f1.backend.domain.game.dto.response;

import java.time.Instant;

public record SystemNoticeResponse(String noticeMessage, Instant timestamp) {

}
