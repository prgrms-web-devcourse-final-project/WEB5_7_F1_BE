package io.f1.backend.domain.user.dto;

import io.f1.backend.domain.user.entity.User;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class UserPrincipal implements UserDetails, OAuth2User {

    public static final String ROLE_USER = "ROLE_USER";
    private final AuthenticationUser authenticationUser;
    private final Map<String, Object> attributes;

    public UserPrincipal(User user, Map<String, Object> attributes) {
        this.authenticationUser = AuthenticationUser.from(user);
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Long getUserId() {
        return authenticationUser.userId();
    }

    public String getUserNickname() {
        return authenticationUser.nickname();
    }

    @Override
    public String getName() {
        return authenticationUser.providerId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(() -> ROLE_USER);
    }

    @Override
    public String getPassword() {
        return null; // 소셜 로그인이라 비밀번호 없음
    }

    @Override
    public String getUsername() {
        return authenticationUser.providerId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
