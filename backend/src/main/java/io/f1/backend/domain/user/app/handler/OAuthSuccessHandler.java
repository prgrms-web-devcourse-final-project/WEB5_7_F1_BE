package io.f1.backend.domain.user.app.handler;

import io.f1.backend.domain.user.dto.UserPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        response.setContentType("application/json;charset=UTF-8");

        if (principal.getUserNickname() == null) {
            String SIGNUP_REDIRECT_URL = "http://localhost:3000/signup";
            getRedirectStrategy().sendRedirect(request, response, SIGNUP_REDIRECT_URL);
        } else {
            String MAIN_REDIRECT_URL = "http://localhost:3000/room";
            getRedirectStrategy().sendRedirect(request, response, MAIN_REDIRECT_URL);
        }
    }
}
