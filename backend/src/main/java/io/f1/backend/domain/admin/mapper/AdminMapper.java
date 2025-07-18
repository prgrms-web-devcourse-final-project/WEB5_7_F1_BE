package io.f1.backend.domain.admin.mapper;

import io.f1.backend.domain.admin.dto.UserPageResponse;
import io.f1.backend.domain.admin.dto.UserResponse;
import org.springframework.data.domain.Page;

public class AdminMapper {

    private AdminMapper() {
    }

    public static UserPageResponse toUserListPageResponse(Page<UserResponse> userPage) {
        int curPage = userPage.getNumber() + 1;

        return new UserPageResponse(
            userPage.getTotalPages(),
            curPage,
            userPage.getNumberOfElements(),
            userPage.getContent()
        );
    }
}
