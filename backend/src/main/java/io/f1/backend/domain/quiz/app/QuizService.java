package io.f1.backend.domain.quiz.app;

import static io.f1.backend.domain.quiz.mapper.QuizMapper.*;

import io.f1.backend.domain.question.app.QuestionService;
import io.f1.backend.domain.question.dto.ContentQuestionRequest;
import io.f1.backend.domain.question.dto.ContentQuestionUpdateRequest;
import io.f1.backend.domain.question.dto.ImageQuestionRequest;
import io.f1.backend.domain.question.dto.ImageQuestionUpdateRequest;
import io.f1.backend.domain.question.dto.QuestionDeleteRequest;
import io.f1.backend.domain.question.dto.TextQuestionRequest;
import io.f1.backend.domain.question.dto.TextQuestionUpdateRequest;
import io.f1.backend.domain.question.entity.Question;
import io.f1.backend.domain.quiz.dao.QuizRepository;
import io.f1.backend.domain.quiz.dto.ImageQuizCreateRequest;
import io.f1.backend.domain.quiz.dto.ImageQuizUpdateRequest;
import io.f1.backend.domain.quiz.dto.QuizCreateRequest;
import io.f1.backend.domain.quiz.dto.QuizCreateResponse;
import io.f1.backend.domain.quiz.dto.QuizListPageResponse;
import io.f1.backend.domain.quiz.dto.QuizListResponse;
import io.f1.backend.domain.quiz.dto.QuizMinData;
import io.f1.backend.domain.quiz.dto.QuizQuestionListResponse;
import io.f1.backend.domain.quiz.dto.QuizUpdateRequest;
import io.f1.backend.domain.quiz.dto.TextQuizCreateRequest;
import io.f1.backend.domain.quiz.dto.TextQuizUpdateRequest;
import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.domain.user.dao.UserRepository;
import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.AuthErrorCode;
import io.f1.backend.global.exception.errorcode.QuestionErrorCode;
import io.f1.backend.global.exception.errorcode.QuizErrorCode;
import io.f1.backend.global.exception.errorcode.UserErrorCode;
import io.f1.backend.global.security.enums.Role;
import io.f1.backend.global.security.util.SecurityUtils;
import io.f1.backend.global.util.FileManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    @Value("${file.thumbnail-path}")
    private String thumbnailPath;

    @Value("${file.question-path}")
    private String questionPath;

    @Value("${file.default-thumbnail-file}")
    private String defaultThumbnailFile;

    private final UserRepository userRepository;
    private final QuestionService questionService;
    private final QuizRepository quizRepository;

    @Transactional
    public QuizCreateResponse saveTextQuiz(
            MultipartFile thumbnailFile, TextQuizCreateRequest request) {
        Quiz savedQuiz = saveQuiz(thumbnailFile, request);

        for (TextQuestionRequest qRequest : request.getQuestions()) {
            questionService.saveContentQuestion(
                    savedQuiz,
                    ContentQuestionRequest.of(qRequest.getContent(), qRequest.getAnswer()));
        }

        return quizToQuizCreateResponse(savedQuiz);
    }

    @Transactional
    public QuizCreateResponse saveImageQuiz(
            MultipartFile thumbnailFile,
            ImageQuizCreateRequest request,
            List<MultipartFile> questionImageFiles) {
        Quiz savedQuiz = saveQuiz(thumbnailFile, request);

        validateImageQuestions(request.getQuestions(), questionImageFiles);

        Iterator<MultipartFile> imageIter = questionImageFiles.iterator();

        for (ImageQuestionRequest qRequest : request.getQuestions()) {
            MultipartFile imageFile = imageIter.next();

            if (hasFile(imageFile)) {
                validateImageFile(imageFile);
            } else {
                throw new CustomException(QuestionErrorCode.INVALID_IMAGE_QUESTION_FILE);
            }

            String imagePath = FileManager.saveMultipartFile(imageFile, questionPath);
            questionService.saveContentQuestion(
                    savedQuiz, ContentQuestionRequest.of(imagePath, qRequest.answer()));
        }

        return quizToQuizCreateResponse(savedQuiz);
    }

    private Quiz saveQuiz(MultipartFile thumbnailFile, QuizCreateRequest request) {
        String savedThumbnailPath = resolveThumbnail(thumbnailFile);
        User creator = loadCreator();
        Quiz quiz = quizCreateRequestToQuiz(request, savedThumbnailPath, creator);

        return quizRepository.save(quiz);
    }

    private String resolveThumbnail(MultipartFile thumbnailFile) {
        String path = thumbnailPath + defaultThumbnailFile;
        if (hasFile(thumbnailFile)) {
            validateImageFile(thumbnailFile);
            path = FileManager.saveMultipartFile(thumbnailFile, thumbnailPath);
        }
        return path;
    }

    private User loadCreator() {
        Long creatorId = SecurityUtils.getCurrentUserId();
        return userRepository
                .findById(creatorId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    }

    private void validateImageQuestions(
            List<ImageQuestionRequest> requestQuestions, List<MultipartFile> questionImageFiles) {
        if (requestQuestions.size() != questionImageFiles.size()) {
            throw new CustomException(QuestionErrorCode.INVALID_IMAGE_QUESTION_SIZE);
        }
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private void validateImageFile(MultipartFile thumbnailFile) {
        if (!thumbnailFile.getContentType().startsWith("image")) {
            throw new CustomException(QuizErrorCode.UNSUPPORTED_MEDIA_TYPE);
        }

        List<String> allowedExt = List.of("jpg", "jpeg", "png", "webp");
        String ext = FileManager.getExtension(thumbnailFile.getOriginalFilename());
        if (!allowedExt.contains(ext)) {
            throw new CustomException(QuizErrorCode.UNSUPPORTED_IMAGE_FORMAT);
        }
    }

    @Transactional
    public void deleteQuiz(Long quizId) {
        Quiz quiz = findQuizWithQuestions(quizId);

        verifyUserAuthority(quiz);

        deleteImageFile(quiz.getThumbnailUrl());

        for (Question question : quiz.getQuestions()) {
            questionService.deleteQuestion(question.getId(), quiz.getQuizType());
        }

        quizRepository.deleteById(quizId);
    }

    @Transactional
    public void deleteQuestions(Long quizId, QuestionDeleteRequest request) {
        Quiz quiz = findQuiz(quizId);

        verifyUserAuthority(quiz);

        for (Long questionId : request.questionIds()) {
            questionService.deleteQuestion(questionId, quiz.getQuizType());
        }
    }

    public static void verifyUserAuthority(Quiz quiz) {
        if (SecurityUtils.getCurrentUserRole() == Role.ADMIN) {
            return;
        }
        if (!Objects.equals(SecurityUtils.getCurrentUserId(), quiz.getCreator().getId())) {
            throw new CustomException(AuthErrorCode.FORBIDDEN);
        }
    }

    @Transactional
    public void updateTextQuiz(
            Long quizId, TextQuizUpdateRequest request, MultipartFile thumbnailFile) {
        Quiz quiz = updateQuiz(quizId, request, thumbnailFile);

        for (TextQuestionUpdateRequest questionReq : request.getQuestions()) {
            questionService.updateContentQuestions(
                    quiz,
                    ContentQuestionUpdateRequest.of(
                            questionReq.getId(),
                            questionReq.getContent(),
                            questionReq.getAnswer()));
        }
    }

    @Transactional
    public void updateImageQuiz(
            Long quizId,
            ImageQuizUpdateRequest request,
            MultipartFile thumbnailFile,
            List<MultipartFile> questionImageFiles) {
        Quiz quiz = updateQuiz(quizId, request, thumbnailFile);

        if (questionImageFiles == null) {
            questionImageFiles = new ArrayList<>();
        }

        Iterator<MultipartFile> fileIter = questionImageFiles.iterator();
        for (ImageQuestionUpdateRequest questionReq : request.getQuestions()) {
            String savedImagePath = null;
            if (questionReq.hasImageFile() && fileIter.hasNext()) {
                MultipartFile imageFile = fileIter.next();
                if (hasFile(imageFile)) {
                    validateImageFile(imageFile);
                } else {
                    throw new CustomException(QuestionErrorCode.INVALID_IMAGE_QUESTION_FILE);
                }
                savedImagePath = FileManager.saveMultipartFile(imageFile, questionPath);
            }
            questionService.updateContentQuestions(
                    quiz,
                    ContentQuestionUpdateRequest.of(
                            questionReq.getId(), savedImagePath, questionReq.getAnswer()));
        }
    }

    private Quiz updateQuiz(Long quizId, QuizUpdateRequest request, MultipartFile thumbnailFile) {
        Quiz quiz = findQuiz(quizId);

        verifyUserAuthority(quiz);

        quiz.changeTitle(request.getTitle());
        quiz.changeDescription(request.getDescription());

        updateThumbnail(quiz, thumbnailFile);

        return quiz;
    }

    private void updateThumbnail(Quiz quiz, MultipartFile thumbnailFile) {
        if (!hasFile(thumbnailFile)) return;
        validateImageFile(thumbnailFile);

        String newThumbnailPath = FileManager.saveMultipartFile(thumbnailFile, thumbnailPath);
        String oldThumbnailPath = quiz.getThumbnailUrl();

        quiz.changeThumbnailUrl(newThumbnailPath);
        deleteImageFile(oldThumbnailPath);
    }

    private void deleteImageFile(String filePath) {
        if (filePath.contains(defaultThumbnailFile)) {
            return;
        }

        FileManager.deleteFile(filePath);
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
        return findQuizWithQuestions(quizId);
    }

    @Transactional(readOnly = true)
    public QuizMinData getQuizMinData() {
        return quizRepository.getQuizMinData();
    }

    @Transactional(readOnly = true)
    public QuizQuestionListResponse getQuizWithQuestions(Long quizId) {
        Quiz quiz = findQuizWithQuestions(quizId);

        return quizToQuizQuestionListResponse(quiz);
    }

    @Transactional(readOnly = true)
    public List<Question> getRandomQuestionsWithoutAnswer(Long quizId, Integer round) {
        findQuiz(quizId);

        return quizRepository.findRandQuestionsByQuizId(quizId, round);
    }

    @Transactional(readOnly = true)
    public Quiz findQuizById(Long quizId) {
        return findQuiz(quizId);
    }

    @Transactional(readOnly = true)
    public Long getQuestionsCount(Long quizId) {
        return quizRepository.countQuestionsByQuizId(quizId);
    }

    private Quiz findQuiz(Long quizId) {
        return quizRepository
                .findById(quizId)
                .orElseThrow(() -> new CustomException(QuizErrorCode.QUIZ_NOT_FOUND));
    }

    private Quiz findQuizWithQuestions(Long quizId) {
        return quizRepository
                .findQuizWithQuestionsById(quizId)
                .orElseThrow(() -> new CustomException(QuizErrorCode.QUIZ_NOT_FOUND));
    }
}
