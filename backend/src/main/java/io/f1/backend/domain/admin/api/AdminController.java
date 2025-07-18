package io.f1.backend.domain.admin.api;

import io.f1.backend.domain.admin.app.AdminService;
import io.f1.backend.domain.admin.dto.UserPageResponse;
import io.f1.backend.global.validation.LimitPageSize;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @LimitPageSize
    @GetMapping("/users")
    public ResponseEntity<UserPageResponse> getUsers(Pageable pageable) {
        UserPageResponse response = adminService.getAllUsers(pageable);
        return ResponseEntity.ok().body(response);
    }
}
