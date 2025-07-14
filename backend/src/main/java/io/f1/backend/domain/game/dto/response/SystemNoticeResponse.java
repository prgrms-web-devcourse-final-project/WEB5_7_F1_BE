package io.f1.backend.domain.game.dto.response;

import java.time.LocalDateTime;

public record SystemNoticeResponse(String noticeMessage, LocalDateTime timestamp) {

}
