package io.f1.backend.domain.game.dto.response;

import io.f1.backend.domain.quiz.dto.GameQuestionResponse;

import java.util.List;

public record GameStartResponse(List<GameQuestionResponse> questions) {}
