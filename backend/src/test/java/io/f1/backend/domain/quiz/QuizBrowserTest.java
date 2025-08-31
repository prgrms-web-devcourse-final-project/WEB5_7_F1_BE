package io.f1.backend.domain.quiz;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;

import io.f1.backend.domain.question.dto.ImageQuestionRequest;
import io.f1.backend.domain.question.dto.TextQuestionRequest;
import io.f1.backend.domain.quiz.dto.ImageQuizCreateRequest;
import io.f1.backend.domain.quiz.dto.TextQuizCreateRequest;
import io.f1.backend.domain.user.dao.UserRepository;
import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.template.BrowserTestTemplate;
import io.f1.backend.global.util.FileManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@WithMockUser
class QuizBrowserTest extends BrowserTestTemplate {

    @Autowired UserRepository userRepository;
    @Autowired ObjectMapper objectMapper;

    @Test
    @DataSet("datasets/user.yml")
    @DisplayName("텍스트 퀴즈를 등록하면 201 응답을 받는다.")
    void createTextQuiz() throws Exception {
        // given
        User user = userRepository.findById(1L).orElseThrow(AssertionError::new);
        MockHttpSession session = getMockSession(user, true);
        MockMultipartFile thumbnailFile = createMockMultipartFile("thumbnailFile");

        List<TextQuestionRequest> questions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            questions.add(new TextQuestionRequest("test", "test"));
        }
        TextQuizCreateRequest request =
                new TextQuizCreateRequest("test", "test description", questions);

        MockPart requestPart = createJsonMockPart("request", request);

        // when
        ResultActions result;
        try (MockedStatic<FileManager> mockFileManager = mockStatic(FileManager.class)) {
            mockFileManager(mockFileManager);
            result =
                    mockMvc.perform(
                            multipart("/quizzes/text")
                                    .file(thumbnailFile)
                                    .part(requestPart)
                                    .session(session));
        }

        // then
        result.andExpectAll(
                status().isCreated(),
                jsonPath("$.title").value("test"),
                jsonPath("$.quizType").value("TEXT"),
                jsonPath("$.description").value("test description"),
                jsonPath("$.thumbnailUrl").value("testpath"),
                jsonPath("$.creatorId").value(1));
    }

    @Test
    @DataSet("datasets/user.yml")
    @DisplayName("이미지 퀴즈를 등록하면 201 응답을 받는다.")
    void createImageQuiz() throws Exception {
        // given
        User user = userRepository.findById(1L).orElseThrow(AssertionError::new);
        MockHttpSession session = getMockSession(user, true);
        MockMultipartFile thumbnailFile = createMockMultipartFile("thumbnailFile");
        List<MockMultipartFile> questionImageFiles = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            questionImageFiles.add(createMockMultipartFile("questionImageFiles"));
        }
        List<ImageQuestionRequest> questions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            questions.add(new ImageQuestionRequest("test"));
        }
        ImageQuizCreateRequest request =
                new ImageQuizCreateRequest("test", "test description", questions);
        MockPart requestPart = createJsonMockPart("request", request);

        // when
        MockMultipartHttpServletRequestBuilder mockMvcBuilder =
                multipart("/quizzes/image").file(thumbnailFile).part(requestPart);

        for (MockMultipartFile questionImageFile : questionImageFiles) {
            mockMvcBuilder.file(questionImageFile);
        }

        mockMvcBuilder.session(session);

        ResultActions result;
        try (MockedStatic<FileManager> mockFileManager = mockStatic(FileManager.class)) {
            mockFileManager(mockFileManager);
            result = mockMvc.perform(mockMvcBuilder);
        }

        // then
        result.andExpectAll(
                status().isCreated(),
                jsonPath("$.title").value("test"),
                jsonPath("$.quizType").value("IMAGE"),
                jsonPath("$.description").value("test description"),
                jsonPath("$.thumbnailUrl").value("testpath"),
                jsonPath("$.creatorId").value(1));
    }

    private MockMultipartFile createMockMultipartFile(String requestPart) {
        return new MockMultipartFile(
                requestPart, "test-image.jpg", "image/jpeg", "test-image.jpg".getBytes());
    }

    private MockPart createJsonMockPart(String requestPart, Object requestObject) throws Exception {
        MockPart part =
                new MockPart(
                        requestPart,
                        objectMapper
                                .writeValueAsString(requestObject)
                                .getBytes(StandardCharsets.UTF_8));
        part.getHeaders().set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return part;
    }

    private void mockFileManager(MockedStatic<FileManager> mockFileManager) {
        mockFileManager
                .when(() -> FileManager.saveMultipartFile(any(MultipartFile.class), anyString()))
                .thenReturn("testpath");
        mockFileManager.when(() -> FileManager.getExtension(anyString())).thenReturn("jpg");
    }
}
