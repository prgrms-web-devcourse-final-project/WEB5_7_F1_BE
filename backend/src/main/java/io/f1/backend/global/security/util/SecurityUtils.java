package io.f1.backend.global.security.util;

import io.f1.backend.domain.admin.dto.AdminPrincipal;
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.AuthErrorCode;
import io.f1.backend.global.security.enums.Role;

import jakarta.servlet.http.HttpSession;

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
        Authentication authentication = getAuthentication();
        if (authentication != null
                && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        }
        throw new CustomException(AuthErrorCode.UNAUTHORIZED);
    }

    public static Long getCurrentUserId() {
        return getCurrentUserPrincipal().getUserId();
    }

    public static String getCurrentUserNickname() {
        return getCurrentUserPrincipal().getUserNickname();
    }

    public static Role getCurrentUserRole() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return Role.USER;
        }
        return Role.ADMIN;
    }

    public static void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        clearAuthentication();
    }

    private static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    public static AdminPrincipal getCurrentAdminPrincipal() {
        Authentication authentication = getAuthentication();
        if (authentication != null
                && authentication.getPrincipal() instanceof AdminPrincipal adminPrincipal) {
            return adminPrincipal;
        }
        throw new CustomException(AuthErrorCode.UNAUTHORIZED);
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
