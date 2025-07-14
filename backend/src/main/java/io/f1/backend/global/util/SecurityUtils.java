package io.f1.backend.global.util;

import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.domain.user.entity.User;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

public class SecurityUtils {

    private SecurityUtils() {}

    public static void setAuthentication(User user) {
        UserPrincipal userPrincipal = new UserPrincipal(user, Collections.emptyMap());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        }
        throw new RuntimeException("E401001: 로그인이 필요합니다.");
    }

    public static Long getCurrentUserId() {
        return getCurrentUserPrincipal().getUserId();
    }

    public static String getCurrentUserNickname() {
        return getCurrentUserPrincipal().getUserNickname();
    }
}
