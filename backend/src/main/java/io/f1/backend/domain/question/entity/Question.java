package io.f1.backend.domain.question.entity;

import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.global.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private String answer;

    @OneToOne(mappedBy = "question", cascade = CascadeType.REMOVE)
    private TextQuestion textQuestion;

    public Question(Quiz quiz, String answer) {
        this.quiz = quiz;
        this.answer = answer;
    }

    public void addTextQuestion(TextQuestion textQuestion) {
        this.textQuestion = textQuestion;
    }
}
