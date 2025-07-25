package io.f1.backend.domain.quiz.dao;

import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.quiz.dto.QuizMinData;
import io.f1.backend.domain.quiz.entity.Quiz;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Page<Quiz> findQuizzesByTitleContaining(String title, Pageable pageable);

    Page<Quiz> findQuizzesByCreator_NicknameContaining(String creator, Pageable pageable);

    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :quizId")
    Optional<Quiz> findQuizWithQuestionsById(Long quizId);

    @Query("SELECT COUNT(qst) FROM Quiz q JOIN q.questions qst WHERE q.id = :quizId")
    Long countQuestionsByQuizId(Long quizId);

    @Query(
"""
    SELECT new io.f1.backend.domain.quiz.dto.QuizMinData (q.id, COUNT(qs.id))
    FROM Quiz q
    LEFT JOIN q.questions qs
    WHERE q.id = (SELECT MIN(q2.id) FROM Quiz q2)
    GROUP BY q.id
""")
    QuizMinData getQuizMinData();

    @Query(
            value = "SELECT * FROM question WHERE quiz_id = :quizId ORDER BY RAND() LIMIT :round",
            nativeQuery = true)
    List<Question> findRandQuestionsByQuizId(Long quizId, Integer round);
}
