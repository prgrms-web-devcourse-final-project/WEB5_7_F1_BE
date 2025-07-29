package io.f1.backend.domain.user.app.handler;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.f1.backend.global.exception.errorcode.AuthErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("code", AuthErrorCode.UNAUTHORIZED.getCode());
        errorResponse.put("message", AuthErrorCode.UNAUTHORIZED.getMessage());

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
