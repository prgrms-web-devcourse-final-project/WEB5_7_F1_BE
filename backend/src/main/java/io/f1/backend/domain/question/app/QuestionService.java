package io.f1.backend.domain.question.app;

import static io.f1.backend.domain.question.mapper.QuestionMapper.questionRequestToQuestion;
import static io.f1.backend.domain.question.mapper.TextQuestionMapper.questionRequestToTextQuestion;

import io.f1.backend.domain.question.dao.QuestionRepository;
import io.f1.backend.domain.question.dao.TextQuestionRepository;
import io.f1.backend.domain.question.dto.QuestionRequest;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.question.entity.TextQuestion;
import io.f1.backend.domain.quiz.entity.Quiz;

import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.QuestionErrorCode;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final TextQuestionRepository textQuestionRepository;

    @Transactional
    public void saveQuestion(Quiz quiz, QuestionRequest request) {

        Question question = questionRequestToQuestion(quiz, request);
        quiz.addQuestion(question);
        questionRepository.save(question);

        TextQuestion textQuestion = questionRequestToTextQuestion(question, request.getContent());
        textQuestionRepository.save(textQuestion);
        question.addTextQuestion(textQuestion);
    }

    @Transactional
    public void updateQuestionContent(Long questionId, String content) {

        validateContent(content);

        Question question =
                questionRepository
                        .findById(questionId)
                        .orElseThrow(() -> new CustomException(QuestionErrorCode.QUESTION_NOT_FOUND));

        TextQuestion textQuestion = question.getTextQuestion();
        textQuestion.changeContent(content);
    }

    @Transactional
    public void updateQuestionAnswer(Long questionId, String answer) {

        validateAnswer(answer);

        Question question =
                questionRepository
                        .findById(questionId)
                        .orElseThrow(() -> new CustomException(QuestionErrorCode.QUESTION_NOT_FOUND));

        question.changeAnswer(answer);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {

        Question question =
                questionRepository
                        .findById(questionId)
                        .orElseThrow(() -> new CustomException(QuestionErrorCode.QUESTION_NOT_FOUND));

        questionRepository.delete(question);
    }

    private void validateAnswer(String answer) {
        if (answer.trim().length() < 5 || answer.trim().length() > 30) {
            throw new CustomException(QuestionErrorCode.INVALID_ANSWER_LENGTH);
        }
    }

    private void validateContent(String content) {
        if (content.trim().length() < 5 || content.trim().length() > 30) {
            throw new CustomException(QuestionErrorCode.INVALID_CONTENT_LENGTH);
        }
    }
}
