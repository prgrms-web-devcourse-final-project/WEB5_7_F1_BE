package io.f1.backend.domain.quiz.api;

import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.quiz.dto.QuizCreateRequest;
import io.f1.backend.domain.quiz.dto.QuizCreateResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuizCreateResponse> saveQuiz(
            @RequestPart(required = false) MultipartFile thumbnailFile,
            @Valid @RequestPart QuizCreateRequest request)
            throws IOException {
        QuizCreateResponse response = quizService.saveQuiz(thumbnailFile, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
