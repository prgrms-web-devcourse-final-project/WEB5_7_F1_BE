package io.f1.backend.domain.question.dto;

import java.util.List;

public record QuestionDeleteRequest(List<Long> questionIds) {}
