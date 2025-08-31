package io.f1.backend.domain.question.dao;

import io.f1.backend.domain.question.entity.Question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q JOIN FETCH q.contentQuestion WHERE q.id = :id")
    Optional<Question> findByIdWithContent(Long id);
}
