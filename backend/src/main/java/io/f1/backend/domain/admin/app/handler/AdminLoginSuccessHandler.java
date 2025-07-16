package io.f1.backend.domain.admin.app.handler;

import static io.f1.backend.domain.user.constants.SessionKeys.ADMIN;
import static io.f1.backend.global.util.SecurityUtils.getCurrentAdminPrincipal;

import io.f1.backend.domain.admin.dto.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final HttpSession httpSession;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication) {

        AdminPrincipal principal = getCurrentAdminPrincipal();
        httpSession.setAttribute(ADMIN, principal.getAuthenticationAdmin());

        response.setStatus(HttpServletResponse.SC_OK); // 200
    }
}
