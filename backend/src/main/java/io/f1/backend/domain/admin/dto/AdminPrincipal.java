package io.f1.backend.domain.admin.dto;

import io.f1.backend.domain.admin.entity.Admin;
import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class AdminPrincipal implements UserDetails {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    private final AuthenticationAdmin authenticationAdmin;
    private final String password;

    public AdminPrincipal(Admin admin) {
        this.authenticationAdmin = AuthenticationAdmin.from(admin);
        this.password = admin.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(() -> ROLE_ADMIN);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return authenticationAdmin.username();
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
