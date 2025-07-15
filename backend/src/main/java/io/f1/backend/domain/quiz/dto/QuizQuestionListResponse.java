package io.f1.backend.domain.quiz.dto;

import io.f1.backend.domain.question.dto.QuestionResponse;
import io.f1.backend.domain.quiz.entity.QuizType;

import java.util.List;

public record QuizQuestionListResponse(
        String title,
        QuizType quizType,
        Long creatorId,
        String description,
        String thumbnailUrl,
        int numberOfQuestion,
        List<QuestionResponse> questions) {}
