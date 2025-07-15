package io.f1.backend.domain.quiz.api;

import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.quiz.dto.QuizCreateRequest;
import io.f1.backend.domain.quiz.dto.QuizCreateResponse;
import io.f1.backend.domain.quiz.dto.QuizListPageResponse;
import io.f1.backend.domain.quiz.dto.QuizQuestionListResponse;
import io.f1.backend.domain.quiz.dto.QuizUpdateRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {

        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{quizId}")
    public ResponseEntity<Void> updateQuiz(
            @PathVariable Long quizId,
            @RequestPart(required = false) MultipartFile thumbnailFile,
            @RequestPart QuizUpdateRequest request)
            throws IOException {

        quizService.updateQuiz(quizId, thumbnailFile, request);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<QuizListPageResponse> getQuizzes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String creator) {

        Pageable pageable =
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        QuizListPageResponse quizzes = quizService.getQuizzes(title, creator, pageable);

        return ResponseEntity.ok().body(quizzes);
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizQuestionListResponse> getQuizWithQuestions(@PathVariable Long quizId) {

        QuizQuestionListResponse response = quizService.getQuizWithQuestions(quizId);

        return ResponseEntity.ok().body(response);
    }
}
