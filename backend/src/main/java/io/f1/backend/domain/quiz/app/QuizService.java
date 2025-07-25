package io.f1.backend.domain.quiz.app;

import static io.f1.backend.domain.quiz.mapper.QuizMapper.*;

import static java.nio.file.Files.deleteIfExists;

import io.f1.backend.domain.question.app.QuestionService;
import io.f1.backend.domain.question.dto.QuestionRequest;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.quiz.dao.QuizRepository;
import io.f1.backend.domain.quiz.dto.QuizCreateRequest;
import io.f1.backend.domain.quiz.dto.QuizCreateResponse;
import io.f1.backend.domain.quiz.dto.QuizListPageResponse;
import io.f1.backend.domain.quiz.dto.QuizListResponse;
import io.f1.backend.domain.quiz.dto.QuizMinData;
import io.f1.backend.domain.quiz.dto.QuizQuestionListResponse;
import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.domain.user.dao.UserRepository;
import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.AuthErrorCode;
import io.f1.backend.global.exception.errorcode.QuizErrorCode;
import io.f1.backend.global.exception.errorcode.UserErrorCode;
import io.f1.backend.global.security.enums.Role;
import io.f1.backend.global.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    @Value("${file.thumbnail-path}")
    private String uploadPath;

    @Value("${file.default-thumbnail-url}")
    private String defaultThumbnailPath;

    private final String DEFAULT = "default";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final UserRepository userRepository;
    private final QuestionService questionService;
    private final QuizRepository quizRepository;

    @Transactional
    public QuizCreateResponse saveQuiz(MultipartFile thumbnailFile, QuizCreateRequest request) {
        String thumbnailPath = defaultThumbnailPath;

        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            validateImageFile(thumbnailFile);
            thumbnailPath = convertToThumbnailPath(thumbnailFile);
        }

        Long creatorId = SecurityUtils.getCurrentUserId();
        User creator =
                userRepository
                        .findById(creatorId)
                        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Quiz quiz = quizCreateRequestToQuiz(request, thumbnailPath, creator);

        Quiz savedQuiz = quizRepository.save(quiz);

        for (QuestionRequest qRequest : request.getQuestions()) {
            questionService.saveQuestion(savedQuiz, qRequest);
        }

        return quizToQuizCreateResponse(savedQuiz);
    }

    private void validateImageFile(MultipartFile thumbnailFile) {

        if (!thumbnailFile.getContentType().startsWith("image")) {
            throw new CustomException(QuizErrorCode.UNSUPPORTED_MEDIA_TYPE);
        }

        List<String> allowedExt = List.of("jpg", "jpeg", "png", "webp");
        String ext = getExtension(thumbnailFile.getOriginalFilename());
        if (!allowedExt.contains(ext)) {
            throw new CustomException(QuizErrorCode.UNSUPPORTED_IMAGE_FORMAT);
        }

        if (thumbnailFile.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(QuizErrorCode.FILE_SIZE_TOO_LARGE);
        }
    }

    private String convertToThumbnailPath(MultipartFile thumbnailFile) {
        String originalFilename = thumbnailFile.getOriginalFilename();
        String ext = getExtension(originalFilename);
        String savedFilename = UUID.randomUUID().toString() + "." + ext;

        try {
            Path savePath = Paths.get(uploadPath, savedFilename).toAbsolutePath();
            thumbnailFile.transferTo(savePath.toFile());
        } catch (IOException e) {
            log.error("썸네일 업로드 중 IOException 발생", e);
            throw new CustomException(QuizErrorCode.THUMBNAIL_SAVE_FAILED);
        }

        return "/images/thumbnail/" + savedFilename;
    }

    private String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    @Transactional
    public void deleteQuiz(Long quizId) {

        Quiz quiz =
                quizRepository
                        .findById(quizId)
                        .orElseThrow(() -> new CustomException(QuizErrorCode.QUIZ_NOT_FOUND));

        verifyUserAuthority(quiz);

        deleteThumbnailFile(quiz.getThumbnailUrl());
        quizRepository.deleteById(quizId);
    }

    private static void verifyUserAuthority(Quiz quiz) {
        if (SecurityUtils.getCurrentUserRole() == Role.ADMIN) {
            return;
        }
        if (!Objects.equals(SecurityUtils.getCurrentUserId(), quiz.getCreator().getId())) {
            throw new CustomException(AuthErrorCode.FORBIDDEN);
        }
    }

    @Transactional
    public void updateQuizTitle(Long quizId, String title) {
        Quiz quiz =
                quizRepository
                        .findById(quizId)
                        .orElseThrow(() -> new CustomException(QuizErrorCode.QUIZ_NOT_FOUND));

        verifyUserAuthority(quiz);

        validateTitle(title);
        quiz.changeTitle(title);
    }

    @Transactional
    public void updateQuizDesc(Long quizId, String description) {

        Quiz quiz =
                quizRepository
                        .findById(quizId)
                        .orElseThrow(() -> new CustomException(QuizErrorCode.QUIZ_NOT_FOUND));

        verifyUserAuthority(quiz);

        validateDesc(description);
        quiz.changeDescription(description);
    }

    @Transactional
    public void updateThumbnail(Long quizId, MultipartFile thumbnailFile) {

        Quiz quiz =
                quizRepository
                        .findById(quizId)
                        .orElseThrow(() -> new CustomException(QuizErrorCode.QUIZ_NOT_FOUND));

        verifyUserAuthority(quiz);

        validateImageFile(thumbnailFile);
        String newThumbnailPath = convertToThumbnailPath(thumbnailFile);

        deleteThumbnailFile(quiz.getThumbnailUrl());
        quiz.changeThumbnailUrl(newThumbnailPath);
    }

    private void validateDesc(String desc) {
        if (desc.trim().length() < 10 || desc.trim().length() > 50) {
            throw new CustomException(QuizErrorCode.INVALID_DESC_LENGTH);
        }
    }

    private void validateTitle(String title) {
        if (title.trim().length() < 2 || title.trim().length() > 30) {
            throw new CustomException(QuizErrorCode.INVALID_TITLE_LENGTH);
        }
    }

    private void deleteThumbnailFile(String oldFilename) {
        if (oldFilename.contains(DEFAULT)) {
            return;
        }

        // oldFilename : /images/thumbnail/123asd.jpg
        // filename : 123asd.jpg
        String filename = oldFilename.substring(oldFilename.lastIndexOf("/") + 1);
        Path filePath = Paths.get(uploadPath, filename).toAbsolutePath();

        try {
            boolean deleted = deleteIfExists(filePath);
            if (deleted) {
                log.info("기존 썸네일 삭제 완료 : {}", filePath);
            } else {
                log.info("기존 썸네일 존재 X : {}", filePath);
            }
        } catch (IOException e) {
            log.error("기존 썸네일 삭제 중 오류 : {}", filePath);
            throw new CustomException(QuizErrorCode.THUMBNAIL_DELETE_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public QuizListPageResponse getQuizzes(String title, String creator, Pageable pageable) {

        Page<Quiz> quizzes;

        // 검색어가 있을 때
        if (!StringUtils.isBlank(title)) {
            quizzes = quizRepository.findQuizzesByTitleContaining(title, pageable);
        } else if (!StringUtils.isBlank(creator)) {
            quizzes = quizRepository.findQuizzesByCreator_NicknameContaining(creator, pageable);
        } else { // 검색어가 없을 때 혹은 빈 문자열일 때
            quizzes = quizRepository.findAll(pageable);
        }

        Page<QuizListResponse> quizListResponses = pageQuizToPageQuizListResponse(quizzes);

        return toQuizListPageResponse(quizListResponses);
    }

    @Transactional(readOnly = true)
    public Quiz getQuizWithQuestionsById(Long quizId) {
        Quiz quiz =
                quizRepository
                        .findQuizWithQuestionsById(quizId)
                        .orElseThrow(() -> new CustomException(QuizErrorCode.QUIZ_NOT_FOUND));
        return quiz;
    }

    @Transactional(readOnly = true)
    public QuizMinData getQuizMinData() {
        return quizRepository.getQuizMinData();
    }

    @Transactional(readOnly = true)
    public QuizQuestionListResponse getQuizWithQuestions(Long quizId) {
        Quiz quiz =
                quizRepository
                        .findById(quizId)
                        .orElseThrow(() -> new CustomException(QuizErrorCode.QUIZ_NOT_FOUND));

        return quizToQuizQuestionListResponse(quiz);
    }

    @Transactional(readOnly = true)
    public List<Question> getRandomQuestionsWithoutAnswer(Long quizId, Integer round) {
        quizRepository
                .findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 퀴즈입니다."));

        return quizRepository.findRandQuestionsByQuizId(quizId, round);
    }

    @Transactional(readOnly = true)
    public Quiz findQuizById(Long quizId) {
        return quizRepository
                .findById(quizId)
                .orElseThrow(() -> new CustomException(QuizErrorCode.QUIZ_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Long getQuestionsCount(Long quizId) {
        return quizRepository.countQuestionsByQuizId(quizId);
    }
}
