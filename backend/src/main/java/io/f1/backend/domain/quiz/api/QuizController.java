package io.f1.backend.domain.quiz.api;

import io.f1.backend.domain.question.dto.QuestionDeleteRequest;
import io.f1.backend.domain.quiz.app.QuizService;
import io.f1.backend.domain.quiz.dto.ImageQuizCreateRequest;
import io.f1.backend.domain.quiz.dto.ImageQuizUpdateRequest;
import io.f1.backend.domain.quiz.dto.QuizCreateResponse;
import io.f1.backend.domain.quiz.dto.QuizListPageResponse;
import io.f1.backend.domain.quiz.dto.QuizQuestionListResponse;
import io.f1.backend.domain.quiz.dto.TextQuizCreateRequest;
import io.f1.backend.domain.quiz.dto.TextQuizUpdateRequest;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.CommonErrorCode;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping(value = "/text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuizCreateResponse> saveQuiz(
            @RequestPart(required = false) MultipartFile thumbnailFile,
            @Valid @RequestPart TextQuizCreateRequest request) {

        QuizCreateResponse response = quizService.saveTextQuiz(thumbnailFile, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuizCreateResponse> saveImageQuiz(
            @RequestPart(required = false) MultipartFile thumbnailFile,
            @RequestPart(required = false) List<MultipartFile> questionImageFiles,
            @Valid @RequestPart ImageQuizCreateRequest request) {

        QuizCreateResponse response =
                quizService.saveImageQuiz(thumbnailFile, request, questionImageFiles);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {

        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{quizId}/questions")
    public ResponseEntity<Void> deleteQuestions(
            @PathVariable Long quizId, @RequestBody QuestionDeleteRequest request) {

        quizService.deleteQuestions(quizId, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/text/{quizId}")
    public ResponseEntity<Void> updateTextQuiz(
            @PathVariable Long quizId,
            @RequestPart(required = false) MultipartFile thumbnailFile,
            @Valid @RequestPart TextQuizUpdateRequest request) {

        quizService.updateTextQuiz(quizId, request, thumbnailFile);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/image/{quizId}")
    public ResponseEntity<Void> updateImageQuiz(
            @PathVariable Long quizId,
            @RequestPart(required = false) MultipartFile thumbnailFile,
            @RequestPart(required = false) List<MultipartFile> questionImageFiles,
            @Valid @RequestPart ImageQuizUpdateRequest request) {

        quizService.updateImageQuiz(quizId, request, thumbnailFile, questionImageFiles);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<QuizListPageResponse> getQuizzes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String creator) {

        if (page <= 0) {
            throw new CustomException(CommonErrorCode.INVALID_PAGINATION);
        }
        if (size <= 0 || size > 100) {
            throw new CustomException(CommonErrorCode.INVALID_PAGINATION);
        }

        Pageable pageable =
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        QuizListPageResponse quizzes = quizService.getQuizzes(title, creator, pageable);

        return ResponseEntity.ok().body(quizzes);
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizQuestionListResponse> getQuizWithQuestions(
            @PathVariable Long quizId) {

        QuizQuestionListResponse response = quizService.getQuizWithQuestions(quizId);

        return ResponseEntity.ok().body(response);
    }
}
