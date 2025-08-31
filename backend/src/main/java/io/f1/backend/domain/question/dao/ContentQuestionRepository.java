package io.f1.backend.domain.question.dao;

import io.f1.backend.domain.question.entity.ContentQuestion;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentQuestionRepository extends JpaRepository<ContentQuestion, Long> {}
