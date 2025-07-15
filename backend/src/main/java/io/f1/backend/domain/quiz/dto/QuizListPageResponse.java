package io.f1.backend.domain.quiz.dto;

import java.util.List;

public record QuizListPageResponse(
        int totalPages, int currentPage, long totalElements, List<QuizListResponse> quiz) {}
