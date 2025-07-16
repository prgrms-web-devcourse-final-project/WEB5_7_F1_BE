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
import io.f1.backend.domain.user.entity.User;

import org.springframework.data.domain.Page;

import java.util.List;

public class QuizMapper {

    // TODO : 이후 파라미터에서 user 삭제하기
    public static Quiz quizCreateRequestToQuiz(
            QuizCreateRequest quizCreateRequest, String imgUrl, User user) {

        return new Quiz(
                quizCreateRequest.getTitle(),
                quizCreateRequest.getDescription(),
                quizCreateRequest.getQuizType(),
                imgUrl,
                user // TODO : 이후 creator에 들어갈 User은 현재 로그인 중인 유저를 가져오도록 변경
                );
    }

    public static QuizCreateResponse quizToQuizCreateResponse(Quiz quiz) {
        // TODO : creatorId 넣어주는 부분에서 Getter를 안 쓰고, 현재 로그인한 유저의 id를 담는 식으로 바꿔도 될 듯
        return new QuizCreateResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getQuizType(),
                quiz.getDescription(),
                quiz.getThumbnailUrl(),
                quiz.getCreator().getId());
    }

    public static QuizListResponse quizToQuizListResponse(Quiz quiz) {
        return new QuizListResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getCreator().getNickname(),
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
                                        question.getTextQuestion().getContent(),
                                        question.getAnswer()))
                .toList();
    }

    public static QuizQuestionListResponse quizToQuizQuestionListResponse(Quiz quiz) {
        return new QuizQuestionListResponse(
                quiz.getTitle(),
                quiz.getQuizType(),
                quiz.getCreator().getId(),
                quiz.getDescription(),
                quiz.getThumbnailUrl(),
                quiz.getQuestions().size(),
                questionsToQuestionResponses(quiz.getQuestions()));
    }

    public static List<GameQuestionResponse> toGameQuestionResponseList(List<Question> questions) {
        return questions.stream()
            .map(QuizMapper::toGameQuestionResponse).toList();
    }

    public static GameQuestionResponse toGameQuestionResponse(Question question) {
        return new GameQuestionResponse(question.getId(), question.getTextQuestion().getContent());
    }

    public static GameStartResponse toGameStartResponse(Quiz quiz) {
        return new GameStartResponse(toGameQuestionResponseList(quiz.getQuestions()));
    }
}
