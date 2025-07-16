package io.f1.backend.domain.admin.app.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.f1.backend.domain.admin.dto.AdminLoginFailResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminLoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json;charset=UTF-8");

        AdminLoginFailResponseDto errorResponse = new AdminLoginFailResponseDto(
            "E401005",
            "아이디 또는 비밀번호가 일치하지 않습니다."
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
