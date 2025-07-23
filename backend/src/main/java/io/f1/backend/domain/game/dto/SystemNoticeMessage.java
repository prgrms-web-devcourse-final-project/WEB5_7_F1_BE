package io.f1.backend.domain.game.dto;

public enum SystemNoticeMessage {
    ENTER(" 님이 입장하셨습니다"),
    EXIT(" 님이 퇴장하셨습니다"),
    CORRECT_ANSWER(" 님 정답입니다 !"),
    TIMEOUT("땡 ~ ⏰ 제한 시간 초과!"),
    RECONNECT(" 님이 재연결 되었습니다.");

    private final String message;

    SystemNoticeMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
