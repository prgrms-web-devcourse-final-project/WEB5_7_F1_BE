package io.f1.backend.domain.admin.app.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.f1.backend.domain.admin.dto.AdminLoginFailResponse;
import io.f1.backend.global.exception.errorcode.AuthErrorCode;
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
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception)
        throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json;charset=UTF-8");

        AdminLoginFailResponse errorResponse =
            new AdminLoginFailResponse(AuthErrorCode.LOGIN_FAILED.getCode(),
                AuthErrorCode.LOGIN_FAILED.getMessage());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
