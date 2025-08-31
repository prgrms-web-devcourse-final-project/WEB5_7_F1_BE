package io.f1.backend.domain.quiz.mapper;

import io.f1.backend.domain.game.dto.response.GameStartResponse;
import io.f1.backend.domain.question.dto.QuestionResponse;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.quiz.dto.GameQuestionResponse;
import io.f1.backend.domain.quiz.dto.QuizCreateRequest;
import io.f1.backend.domain.quiz.dto.QuizCreateResponse;
import io.f1.backend.domain.quiz.dto.QuizListPageResponse;
import io.f1.backend.domain.quiz.dto.QuizListResponse;
import io.f1.backend.domain.quiz.dto.QuizQuestionListResponse;
import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.domain.quiz.entity.QuizType;
import io.f1.backend.domain.user.entity.User;

import org.springframework.data.domain.Page;

import java.util.List;

public class QuizMapper {

    public static Quiz quizCreateRequestToQuiz(
            QuizCreateRequest quizCreateRequest, String imgUrl, User creator) {

        return new Quiz(
                quizCreateRequest.getTitle(),
                quizCreateRequest.getDescription(),
                quizCreateRequest.getQuizType(),
                imgUrl,
                creator);
    }

    public static QuizCreateResponse quizToQuizCreateResponse(Quiz quiz) {
        return new QuizCreateResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getQuizType(),
                quiz.getDescription(),
                quiz.getThumbnailUrl(),
                quiz.findCreatorId());
    }

    public static QuizListResponse quizToQuizListResponse(Quiz quiz) {
        return new QuizListResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getQuizType(),
                quiz.findCreatorNickname(),
                quiz.getQuestions().size(),
                quiz.getThumbnailUrl());
    }

    public static QuizListPageResponse toQuizListPageResponse(Page<QuizListResponse> quizzes) {
        return new QuizListPageResponse(
                quizzes.getTotalPages(),
                quizzes.getNumber() + 1,
                quizzes.getTotalElements(),
                quizzes.getContent());
    }

    public static Page<QuizListResponse> pageQuizToPageQuizListResponse(Page<Quiz> quizzes) {
        return quizzes.map(QuizMapper::quizToQuizListResponse);
    }

    public static List<QuestionResponse> questionsToQuestionResponses(List<Question> questions) {
        return questions.stream()
                .map(
                        question ->
                                new QuestionResponse(
                                        question.getId(),
                                        question.getContentQuestion().getContent(),
                                        question.getAnswer()))
                .toList();
    }

    public static QuizQuestionListResponse quizToQuizQuestionListResponse(Quiz quiz) {
        return new QuizQuestionListResponse(
                quiz.getTitle(),
                quiz.getQuizType(),
                quiz.findCreatorId(),
                quiz.getDescription(),
                quiz.getThumbnailUrl(),
                quiz.getQuestions().size(),
                questionsToQuestionResponses(quiz.getQuestions()));
    }

    public static List<GameQuestionResponse> toGameQuestionResponseList(List<Question> questions) {
        return questions.stream().map(QuizMapper::toGameQuestionResponse).toList();
    }

    public static GameQuestionResponse toGameQuestionResponse(Question question) {
        return new GameQuestionResponse(
                question.getId(), question.getContentQuestion().getContent());
    }

    public static GameStartResponse toGameStartResponse(
            QuizType quizType, List<Question> questions) {
        return new GameStartResponse(quizType, toGameQuestionResponseList(questions));
    }
}
