package io.f1.backend.domain.user.app;


import io.f1.backend.domain.user.dao.UserRepository;
import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.UserErrorCode;
import io.f1.backend.global.security.util.SecurityUtils;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("!prod")
@RequiredArgsConstructor
public class TestUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public void login(Long userId, HttpSession session) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        SecurityUtils.setAuthentication(user);
        SecurityContext context = SecurityContextHolder.getContext();
        session.setAttribute("SPRING_SECURITY_CONTEXT", context);
    }
}
