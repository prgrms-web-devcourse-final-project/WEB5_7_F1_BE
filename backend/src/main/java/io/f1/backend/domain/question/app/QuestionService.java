package io.f1.backend.domain.question.app;

import static io.f1.backend.domain.question.mapper.QuestionMapper.questionRequestToQuestion;
import static io.f1.backend.domain.question.mapper.TextQuestionMapper.questionRequestToTextQuestion;

import io.f1.backend.domain.question.dao.QuestionRepository;
import io.f1.backend.domain.question.dao.TextQuestionRepository;
import io.f1.backend.domain.question.dto.QuestionRequest;
import io.f1.backend.domain.question.dto.QuestionUpdateRequest;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.question.entity.TextQuestion;
import io.f1.backend.domain.quiz.entity.Quiz;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void updateQuestion(Long questionId, QuestionUpdateRequest request) {

        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 문제입니다."));

        TextQuestion textQuestion = question.getTextQuestion();

        if(request.content() != null) {
            validateContent(request.content());
            textQuestion.changeContent(request.content());
        }

        if(request.answer() != null) {
            validateAnswer(request.answer());
            question.changeAnswer(request.answer());
        }

    }

    @Transactional
    public void deleteQuestion(Long questionId) {

        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 문제입니다."));

        questionRepository.delete(question);

    }

    private void validateAnswer(String answer) {
        if(answer.trim().length() < 5 || answer.trim().length() > 30) {
            throw new IllegalArgumentException("정답은 1자 이상 30자 이하로 입력해주세요.");
        }
    }

    private void validateContent(String content) {
        if(content.trim().length() < 5 || content.trim().length() > 30) {
            throw new IllegalArgumentException("문제는 5자 이상 30자 이하로 입력해주세요.");
        }
    }
}
