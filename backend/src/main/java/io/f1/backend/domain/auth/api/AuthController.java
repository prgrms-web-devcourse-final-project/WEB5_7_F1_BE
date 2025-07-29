package io.f1.backend.domain.auth.api;

import static io.f1.backend.global.security.util.SecurityUtils.getAuthentication;

import io.f1.backend.domain.admin.dto.AdminPrincipal;
import io.f1.backend.domain.auth.dto.CurrentUserAndAdminResponse;
import io.f1.backend.domain.user.dto.UserPrincipal;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserOrAdmin() {
        Authentication authentication = getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal userPrincipal) {
            return ResponseEntity.ok(CurrentUserAndAdminResponse.from(userPrincipal));
        }
        return ResponseEntity.ok(CurrentUserAndAdminResponse.from((AdminPrincipal) principal));
    }
}
