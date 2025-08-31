package io.f1.backend.domain.question.app;

import io.f1.backend.domain.question.dao.ContentQuestionRepository;
import io.f1.backend.domain.question.dao.QuestionRepository;
import io.f1.backend.domain.question.dto.ContentQuestionRequest;
import io.f1.backend.domain.question.dto.ContentQuestionUpdateRequest;
import io.f1.backend.domain.question.entity.ContentQuestion;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.domain.quiz.entity.QuizType;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.QuestionErrorCode;
import io.f1.backend.global.util.FileManager;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ContentQuestionRepository contentQuestionRepository;

    public void saveContentQuestion(Quiz quiz, ContentQuestionRequest request) {
        Question question = new Question(quiz, request.getAnswer());
        quiz.addQuestion(question);
        questionRepository.save(question);

        ContentQuestion contentQuestion = request.toContentQuestion(question);
        contentQuestionRepository.save(contentQuestion);
        question.addContentQuestion(contentQuestion);
    }

    public void updateContentQuestions(Quiz quiz, ContentQuestionUpdateRequest request) {
        if (request.getId() == null) {
            saveContentQuestion(
                    quiz, ContentQuestionRequest.of(request.getContent(), request.getAnswer()));

            return;
        }

        Question question = getQuestionWithContent(request.getId());

        if (request.getContent() != null) {
            ContentQuestion contentQuestion = question.getContentQuestion();
            contentQuestion.changeContent(request.getContent());
        }

        question.changeAnswer(request.getAnswer());
    }

    public void deleteQuestion(Long questionId, QuizType quizType) {
        Question question;

        if (quizType.name().equals("IMAGE")) {
            question = getQuestionWithContent(questionId);
            String filePath = question.getContentQuestion().getContent();
            FileManager.deleteFile(filePath);
        } else {
            question = getQuestion(questionId);
        }

        questionRepository.delete(question);
    }

    private Question getQuestion(Long questionId) {
        return questionRepository
                .findById(questionId)
                .orElseThrow(() -> new CustomException(QuestionErrorCode.QUESTION_NOT_FOUND));
    }

    private Question getQuestionWithContent(Long questionId) {
        return questionRepository
                .findByIdWithContent(questionId)
                .orElseThrow(() -> new CustomException(QuestionErrorCode.QUESTION_NOT_FOUND));
    }
}
