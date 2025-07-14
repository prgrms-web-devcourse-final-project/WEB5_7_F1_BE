package io.f1.backend.domain.quiz.mapper;

import io.f1.backend.domain.quiz.dto.QuizCreateRequest;
import io.f1.backend.domain.quiz.dto.QuizCreateResponse;
import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.domain.user.entity.User;

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
}
