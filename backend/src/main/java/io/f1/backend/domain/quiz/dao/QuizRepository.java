package io.f1.backend.domain.quiz.dao;

import io.f1.backend.domain.quiz.entity.Quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("SELECT MIN(q.id) FROM Quiz q")
    Long getQuizMinId();
}
