package io.f1.backend.domain.question.dao;

import io.f1.backend.domain.question.entity.TextQuestion;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TextQuestionRepository extends JpaRepository<TextQuestion, Long> {}
