package io.f1.backend.domain.user.api;

import static io.f1.backend.global.util.SecurityUtils.logout;

import io.f1.backend.domain.user.app.UserService;
import io.f1.backend.domain.user.dto.MyPageInfo;
import io.f1.backend.domain.user.dto.SignupRequest;
import io.f1.backend.domain.user.dto.UserPrincipal;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/me")
public class UserController {

    private final UserService userService;

    @DeleteMapping
    public ResponseEntity<Void> deleteCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal, HttpSession httpSession) {
        userService.deleteUser(userPrincipal.getUserId());
        logout(httpSession);
        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateNickname(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody SignupRequest signupRequest,
            HttpSession httpSession) {
        userService.updateNickname(
                userPrincipal.getUserId(), signupRequest.nickname(), httpSession);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<MyPageInfo> getMyPage(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        MyPageInfo response = userService.getMyPage(userPrincipal);
        return ResponseEntity.ok(response);
    }
}
