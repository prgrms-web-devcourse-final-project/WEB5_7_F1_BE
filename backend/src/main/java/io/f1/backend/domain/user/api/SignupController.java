package io.f1.backend.domain.user.api;

import io.f1.backend.domain.user.app.UserService;
import io.f1.backend.domain.user.dto.SignupRequestDto;
import io.f1.backend.domain.user.dto.SignupResponseDto;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SignupController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> completeSignup(
            @RequestBody SignupRequestDto signupRequest, HttpSession httpSession) {
        SignupResponseDto response = userService.signup(httpSession, signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
