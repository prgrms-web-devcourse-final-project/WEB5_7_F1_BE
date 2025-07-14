package io.f1.backend.domain.user.app.handler;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.f1.backend.domain.user.dto.UserPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        response.setContentType("application/json;charset=UTF-8");

        if (principal.getUserNickname() == null) {
            // 닉네임 설정 필요 → 202 Accepted
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "닉네임을 설정하세요."));
        } else {
            // 정상 로그인 → 200 OK
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "로그인 성공"));
        }
    }
}
