package io.f1.backend.domain.admin.dto;

import java.util.List;

public record UserPageResponse(
        int totalPages, int currentPage, int totalElements, List<UserResponse> users) {}
