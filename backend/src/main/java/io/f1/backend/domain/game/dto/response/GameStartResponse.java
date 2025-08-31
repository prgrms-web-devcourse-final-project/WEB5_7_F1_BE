package io.f1.backend.domain.game.dto.response;

import io.f1.backend.domain.quiz.dto.GameQuestionResponse;
import io.f1.backend.domain.quiz.entity.QuizType;

import java.util.List;

public record GameStartResponse(QuizType quizType, List<GameQuestionResponse> questions) {}
