package io.f1.backend.domain.user.app;

import static io.f1.backend.domain.user.constants.SessionKeys.OAUTH_USER;
import static io.f1.backend.domain.user.constants.SessionKeys.USER;
import static io.f1.backend.global.util.RedisPublisher.USER_DELETE;
import static io.f1.backend.global.util.RedisPublisher.USER_NEW;
import static io.f1.backend.global.util.RedisPublisher.USER_UPDATE;

import io.f1.backend.domain.auth.dto.CurrentUserAndAdminResponse;
import io.f1.backend.domain.stat.dao.StatRepository;
import io.f1.backend.domain.user.dao.UserRepository;
import io.f1.backend.domain.user.dto.AuthenticationUser;
import io.f1.backend.domain.user.dto.MyPageInfo;
import io.f1.backend.domain.user.dto.SignupRequest;
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.domain.user.dto.UserSummary;
import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.AuthErrorCode;
import io.f1.backend.global.exception.errorcode.UserErrorCode;
import io.f1.backend.global.util.RedisPublisher;
import io.f1.backend.global.security.util.SecurityUtils;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RedisPublisher redisPublisher;
    private final StatRepository statRepository;

    @Transactional
    public CurrentUserAndAdminResponse signup(HttpSession session, SignupRequest signupRequest) {
        AuthenticationUser authenticationUser = extractSessionUser(session);
        String nickname = signupRequest.nickname();

        checkNickname(nickname);

        User user = initNickname(authenticationUser.userId(), nickname);
        updateSessionAfterSignup(session, user);

        SecurityUtils.setAuthentication(user);
        UserPrincipal userPrincipal = SecurityUtils.getCurrentUserPrincipal();

        redisPublisher.publish(USER_NEW, new UserSummary(user.getId(), nickname));

        return CurrentUserAndAdminResponse.from(userPrincipal);
    }

    private AuthenticationUser extractSessionUser(HttpSession session) {
        AuthenticationUser authenticationUser =
                (AuthenticationUser) session.getAttribute(OAUTH_USER);
        if (authenticationUser == null) {
            throw new CustomException(AuthErrorCode.UNAUTHORIZED);
        }
        return authenticationUser;
    }

    private void validateNicknameFormat(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new CustomException(UserErrorCode.NICKNAME_EMPTY);
        }
        if (nickname.length() > 6) {
            throw new CustomException(UserErrorCode.NICKNAME_TOO_LONG);
        }
        if (!nickname.matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new CustomException(UserErrorCode.NICKNAME_NOT_ALLOWED);
        }
    }

    @Transactional(readOnly = true)
    public void validateNicknameDuplicate(String nickname) {
        if (userRepository.existsUserByNicknameIgnoreCase(nickname)) {
            throw new CustomException(UserErrorCode.NICKNAME_CONFLICT);
        }
    }

    @Transactional
    public User initNickname(Long userId, String nickname) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        user.updateNickname(nickname);

        return userRepository.save(user);
    }

    private void updateSessionAfterSignup(HttpSession session, User user) {
        session.removeAttribute(OAUTH_USER);
        session.setAttribute(USER, AuthenticationUser.from(user));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);

        redisPublisher.publish(USER_DELETE, userId.toString());
    }

    @Transactional
    public void updateNickname(Long userId, String newNickname, HttpSession session) {
        checkNickname(newNickname);

        User user = initNickname(userId, newNickname);
        session.setAttribute(USER, AuthenticationUser.from(user));
        SecurityUtils.setAuthentication(user);

        redisPublisher.publish(USER_UPDATE, new UserSummary(user.getId(), newNickname));
    }

    @Transactional(readOnly = true)
    public void checkNickname(String nickname) {
        validateNicknameFormat(nickname);
        validateNicknameDuplicate(nickname);
    }

    @Transactional(readOnly = true)
    public MyPageInfo getMyPage(UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getUserId();
        return statRepository.getMyPageByUserId(userId);
    }
}
