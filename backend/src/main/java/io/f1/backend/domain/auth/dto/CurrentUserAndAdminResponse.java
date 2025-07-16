package io.f1.backend.domain.auth.dto;

import io.f1.backend.domain.admin.dto.AdminPrincipal;
import io.f1.backend.domain.user.dto.UserPrincipal;

public record CurrentUserAndAdminResponse(Long id, String name, String role) {

    public static CurrentUserAndAdminResponse from(UserPrincipal userPrincipal) {
        return new CurrentUserAndAdminResponse(
            userPrincipal.getUserId(),
            userPrincipal.getUserNickname(),
            UserPrincipal.ROLE_USER
        );
    }

    public static CurrentUserAndAdminResponse from(AdminPrincipal adminPrincipal) {
        return new CurrentUserAndAdminResponse(
            adminPrincipal.getAuthenticationAdmin().adminId(),
            adminPrincipal.getUsername(),
            AdminPrincipal.ROLE_ADMIN
        );
    }
}
