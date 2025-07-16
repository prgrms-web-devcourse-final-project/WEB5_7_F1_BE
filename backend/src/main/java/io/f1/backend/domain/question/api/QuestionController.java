package io.f1.backend.domain.question.api;

import io.f1.backend.domain.question.app.QuestionService;
import io.f1.backend.domain.question.dto.QuestionUpdateRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PutMapping("/{questionId}")
    public ResponseEntity<Void> updateQuestion(
            @PathVariable Long questionId, @RequestBody QuestionUpdateRequest request) {

        if (request.content() != null) {
            questionService.updateQuestionContent(questionId, request.content());
        }

        if (request.content() != null) {
            questionService.updateQuestionAnswer(questionId, request.answer());
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);

        return ResponseEntity.noContent().build();
    }
}
