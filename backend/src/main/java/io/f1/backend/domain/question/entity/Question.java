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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
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
    private ContentQuestion contentQuestion;

    public Question(Quiz quiz, String answer) {
        this.quiz = quiz;
        this.answer = answer;
    }

    public void addContentQuestion(ContentQuestion contentQuestion) {
        this.contentQuestion = contentQuestion;
    }

    public void changeAnswer(String answer) {
        this.answer = answer;
    }
}
