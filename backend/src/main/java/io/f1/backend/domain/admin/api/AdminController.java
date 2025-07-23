package io.f1.backend.domain.admin.api;

import io.f1.backend.domain.admin.app.AdminService;
import io.f1.backend.domain.admin.dto.UserPageResponse;
import io.f1.backend.global.validation.LimitPageSize;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @LimitPageSize
    @GetMapping("/users")
    public ResponseEntity<UserPageResponse> getUsers(
            @RequestParam(required = false) String nickname, Pageable pageable) {
        UserPageResponse response;

        if (StringUtils.isBlank(nickname)) {
            response = adminService.getAllUsers(pageable);
        } else {
            response = adminService.searchUsersByNickname(nickname, pageable);
        }
        return ResponseEntity.ok().body(response);
    }
}
