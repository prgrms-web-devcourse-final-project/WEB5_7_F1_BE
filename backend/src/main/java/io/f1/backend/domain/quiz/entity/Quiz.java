package io.f1.backend.domain.quiz.entity;

import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter // quizService의 퀴즈 조회 메서드 구현 시까지 임시 사용
@Entity
@NoArgsConstructor
public class Quiz extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.REMOVE)
    private List<Question> questions = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizType quizType;

    @Column(nullable = false)
    private String thumbnailUrl;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    public Quiz(
            String title,
            String description,
            QuizType quizType,
            String thumbnailUrl,
            User creator) {
        this.title = title;
        this.description = description;
        this.quizType = quizType;
        this.thumbnailUrl = thumbnailUrl;
        this.creator = creator;
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }
}
