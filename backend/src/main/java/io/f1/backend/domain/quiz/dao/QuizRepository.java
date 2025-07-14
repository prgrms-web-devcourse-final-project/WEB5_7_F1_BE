package io.f1.backend.domain.quiz.dao;

import io.f1.backend.domain.quiz.entity.Quiz;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {}
