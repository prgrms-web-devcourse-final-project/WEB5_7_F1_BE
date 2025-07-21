package io.f1.backend.domain.game.dto;

public enum RoomEventType {
    ENTER(SystemNoticeMessage.ENTER),
    EXIT(SystemNoticeMessage.EXIT),
    START(null),
    END(null),
    CORRECT_ANSWER(SystemNoticeMessage.CORRECT_ANSWER),
    TIMEOUT(SystemNoticeMessage.TIMEOUT),
    RECONNECT(null);

    private final SystemNoticeMessage systemMessage;

    RoomEventType(SystemNoticeMessage systemMessage) {
        this.systemMessage = systemMessage;
    }

    public String getMessage(String nickname) {

        if (this == TIMEOUT) {
            return systemMessage.getMessage();
        }

        return nickname + systemMessage.getMessage();
    }
}
