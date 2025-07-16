package io.f1.backend.domain.user.api;

import io.f1.backend.domain.user.app.UserService;
import io.f1.backend.domain.user.dto.SignupRequestDto;
import io.f1.backend.domain.user.dto.SignupResponseDto;

import io.f1.backend.domain.user.dto.TestLoginRequest;
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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

    @PostMapping("/test-login")
    public String loginForTest(@RequestBody TestLoginRequest request, HttpServletRequest httpRequest) {

        User user = new User();
        user.setId(request.userId());
        user.setNickname(request.nickname());

        // SecurityUtils에 위임
        SecurityUtils.setAuthentication(user);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.getContext()
        );
        session.setAttribute("user", user);

        return "로그인 성공";
    }
}
