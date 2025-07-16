package io.f1.backend.domain.game.dto;

import io.f1.backend.domain.game.dto.response.QuestionResultResponse;
import io.f1.backend.domain.game.dto.response.RankUpdateResponse;
import io.f1.backend.domain.game.dto.response.SystemNoticeResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RoundResult {
    String destination;
    QuestionResultResponse questionResult;
    RankUpdateResponse rankUpdate;
    SystemNoticeResponse systemNotice;
    ChatMessage chat;

    @Builder
    public RoundResult(String destination,QuestionResultResponse questionResult, RankUpdateResponse rankUpdate,
        SystemNoticeResponse systemNotice, ChatMessage chat) {
        this.questionResult = questionResult;
        this.rankUpdate = rankUpdate;
        this.systemNotice = systemNotice;
        this.chat = chat;
    }

    public boolean hasChat(){
        return chat != null;
    }

    public boolean hasOnlyChat() {
        return chat != null
            && questionResult == null
            && rankUpdate == null
            && systemNotice == null;
    }

}
