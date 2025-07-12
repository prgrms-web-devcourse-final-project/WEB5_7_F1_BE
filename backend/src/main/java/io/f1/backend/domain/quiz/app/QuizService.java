package io.f1.backend.domain.quiz.app;

import io.f1.backend.domain.question.app.QuestionService;
import io.f1.backend.domain.question.dto.QuestionRequest;
import io.f1.backend.domain.quiz.dao.QuizRepository;
import io.f1.backend.domain.quiz.dto.QuizCreateRequest;
import io.f1.backend.domain.quiz.dto.QuizCreateResponse;
import io.f1.backend.domain.quiz.entity.Quiz;
import io.f1.backend.domain.quiz.mapper.QuizMapper;
import io.f1.backend.domain.user.dao.UserRepository;
import io.f1.backend.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizService {

    @Value("${file.thumbnail-path}")
    private String uploadPath;

    @Value("${file.default-thumbnail-url}")
    private String defaultThumbnailPath;

    // TODO : 시큐리티 구현 이후 삭제해도 되는 의존성 주입
    private final UserRepository userRepository;
    private final QuestionService questionService;
    private final QuizRepository quizRepository;

    @Transactional
    public QuizCreateResponse saveQuiz(MultipartFile file, QuizCreateRequest request)
            throws IOException {
        String imgUrl = defaultThumbnailPath;

        if (file != null && !file.isEmpty()) {
            validateImageFile(file);
            imgUrl = saveThumbnail(file);
        }

        // TODO : 시큐리티 구현 이후 삭제 (data.sql로 초기 저장해둔 유저 get), 나중엔 현재 로그인한 유저의 아이디를 받아오도록 수정
        User user = userRepository.findById(1L).orElseThrow(RuntimeException::new);

        Quiz quiz = QuizMapper.quizCreateRequestToQuiz(request, imgUrl, user);

        Quiz savedQuiz = quizRepository.save(quiz);

        for (QuestionRequest qRequest : request.getQuestions()) {
            questionService.saveQuestion(savedQuiz, qRequest);
        }

        return QuizMapper.quizToQuizCreateResponse(savedQuiz);
    }

    private void validateImageFile(MultipartFile file) {

        if (!file.getContentType().startsWith("image")) {
            // TODO : 이후 커스텀 예외로 변경
            throw new IllegalArgumentException("이미지 파일을 업로드해주세요.");
        }

        List<String> allowedExt = List.of("jpg", "jpeg", "png", "webp");
        if (!allowedExt.contains(getExtension(file.getOriginalFilename()))) {
            throw new IllegalArgumentException("지원하지 않는 확장자입니다.");
        }
    }

    private String saveThumbnail(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String ext = getExtension(originalFilename);
        String savedFilename = UUID.randomUUID().toString() + "." + ext;

        Path savePath = Paths.get(uploadPath, savedFilename).toAbsolutePath();
        file.transferTo(savePath.toFile());

        return "/images/thumbnail/" + savedFilename;
    }

    private String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
