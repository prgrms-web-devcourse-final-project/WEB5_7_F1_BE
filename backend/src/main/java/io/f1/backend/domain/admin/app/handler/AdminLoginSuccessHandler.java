package io.f1.backend.domain.admin.app.handler;

import static io.f1.backend.domain.user.constants.SessionKeys.ADMIN;
import static io.f1.backend.global.util.SecurityUtils.getCurrentAdminPrincipal;

import io.f1.backend.domain.admin.dao.AdminRepository;
import io.f1.backend.domain.admin.dto.AdminPrincipal;
import io.f1.backend.domain.admin.entity.Admin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AdminRepository adminRepository;
    private final HttpSession httpSession;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication) {

        AdminPrincipal principal = getCurrentAdminPrincipal();
        Admin admin = adminRepository.findByUsername(principal.getUsername())
            .orElseThrow(() -> new RuntimeException("E404007: 존재하지 않는 관리자입니다."));

        admin.updateLastLogin(LocalDateTime.now());
        adminRepository.save(admin);
        httpSession.setAttribute(ADMIN, principal.getAuthenticationAdmin());

        response.setStatus(HttpServletResponse.SC_OK); // 200
    }
}
