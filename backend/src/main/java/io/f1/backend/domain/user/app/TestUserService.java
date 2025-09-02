package io.f1.backend.domain.user.app;

import static io.f1.backend.domain.user.constants.SessionKeys.USER;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.f1.backend.domain.user.dao.UserRepository;
import io.f1.backend.domain.user.dto.AuthenticationUser;
import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.UserErrorCode;
import io.f1.backend.global.security.util.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@Profile("!prod")
@RequiredArgsConstructor
public class TestUserService {
    
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public void login(Long userId, HttpSession session) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        session.setAttribute(USER, AuthenticationUser.from(user));
        SecurityUtils.setAuthentication(user);
    }
}
