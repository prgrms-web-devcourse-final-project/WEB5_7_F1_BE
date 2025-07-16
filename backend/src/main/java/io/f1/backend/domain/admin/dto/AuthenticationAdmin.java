package io.f1.backend.domain.admin.dto;

import io.f1.backend.domain.admin.entity.Admin;

public record AuthenticationAdmin(Long adminId, String username) {

    public static AuthenticationAdmin from(Admin admin) {
        return new AuthenticationAdmin(admin.getId(), admin.getUsername());
    }
}
