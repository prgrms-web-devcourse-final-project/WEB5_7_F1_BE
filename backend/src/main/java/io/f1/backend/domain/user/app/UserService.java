package io.f1.backend.domain.user.app;

import io.f1.backend.domain.user.dao.UserRepository;
import io.f1.backend.domain.user.dto.SessionUser;
import io.f1.backend.domain.user.dto.SignupRequestDto;
import io.f1.backend.domain.user.dto.SignupResponseDto;
import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.util.SecurityUtils;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public SignupResponseDto signup(HttpSession session, SignupRequestDto signupRequest) {
        SessionUser sessionUser = extractSessionUser(session);

        String nickname = signupRequest.nickname();
        validateNickname(nickname);
        validateDuplicateNickname(nickname);

        User user = updateUserNickname(sessionUser.getUserId(), nickname);
        updateSessionAfterSignup(session, user);
        SecurityUtils.setAuthentication(user);

        return SignupResponseDto.toDto(user);
    }

    private SessionUser extractSessionUser(HttpSession session) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("OAuthUser");
        if (sessionUser == null) {
            throw new RuntimeException("세션에 OAuth 정보 없음");
        }
        return sessionUser;
    }

    private void validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new RuntimeException("E400002: 닉네임은 필수 입력입니다.");
        }
        if (nickname.length() > 6) {
            throw new RuntimeException("E400003: 닉네임은 6글자 이하로 입력해야 합니다.");
        }
        if (!nickname.matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new RuntimeException("E400004: 한글, 영문, 숫자만 입력해주세요.");
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (userRepository.existsUserByNickname(nickname)) {
            throw new RuntimeException("닉네임 중복");
        }
    }

    private User updateUserNickname(Long userId, String nickname) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자 없음"));
        user.updateNickname(nickname);

        return userRepository.save(user);
    }

    private void updateSessionAfterSignup(HttpSession session, User user) {
        session.removeAttribute("OAuthUser");
        session.setAttribute("user", new SessionUser(user));
    }
}
