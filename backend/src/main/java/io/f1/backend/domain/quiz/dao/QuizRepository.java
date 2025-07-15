package io.f1.backend.domain.quiz.dao;

import io.f1.backend.domain.quiz.entity.Quiz;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Page<Quiz> findQuizzesByTitleContaining(String title, Pageable pageable);

    Page<Quiz> findQuizzesByCreator_NicknameContaining(String creator, Pageable pageable);

    @Query("SELECT MIN(q.id) FROM Quiz q")
    Long getQuizMinId();
}
