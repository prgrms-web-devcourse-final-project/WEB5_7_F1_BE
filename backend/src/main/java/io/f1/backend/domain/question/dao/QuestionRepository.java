package io.f1.backend.domain.question.dao;

import io.f1.backend.domain.question.entity.Question;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {}
