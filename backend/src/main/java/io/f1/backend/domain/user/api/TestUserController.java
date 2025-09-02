package io.f1.backend.domain.user.api;

import io.f1.backend.domain.user.app.TestUserService;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("!prod")
@RequiredArgsConstructor
@RequestMapping("/user/test")
public class TestUserController {

    private final TestUserService userService;

    @PostMapping("/login/{userId}")
    public ResponseEntity<Void> login(@PathVariable Long userId, HttpSession session) {
        userService.login(userId, session);
        return ResponseEntity.ok().build();
    }
}
