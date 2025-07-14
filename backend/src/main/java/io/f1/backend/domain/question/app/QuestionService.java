package io.f1.backend.domain.question.app;

import static io.f1.backend.domain.question.mapper.QuestionMapper.questionRequestToQuestion;
import static io.f1.backend.domain.question.mapper.TextQuestionMapper.questionRequestToTextQuestion;

import io.f1.backend.domain.question.dao.QuestionRepository;
import io.f1.backend.domain.question.dao.TextQuestionRepository;
import io.f1.backend.domain.question.dto.QuestionRequest;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.question.entity.TextQuestion;
import io.f1.backend.domain.quiz.entity.Quiz;

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
}
