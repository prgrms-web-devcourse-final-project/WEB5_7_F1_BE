package io.f1.backend.domain.user.api;

import io.f1.backend.domain.auth.dto.CurrentUserAndAdminResponse;
import io.f1.backend.domain.user.app.UserService;
import io.f1.backend.domain.user.dto.SignupRequest;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SignupController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<CurrentUserAndAdminResponse> completeSignup(
            @RequestBody SignupRequest signupRequest, HttpSession httpSession) {
        CurrentUserAndAdminResponse response = userService.signup(httpSession, signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Void> checkNicknameDuplicate(@RequestParam String nickname) {
        userService.checkNickname(nickname);
        return ResponseEntity.ok().build();
    }
}
