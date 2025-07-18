package io.f1.backend.global.filter;

import io.f1.backend.domain.admin.dto.AdminPrincipal;
import io.f1.backend.domain.admin.entity.Admin;
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.domain.user.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class DevTokenAuthFilter extends OncePerRequestFilter {

    private static final String DEV_TOKEN = "dev-secret-token-1234";
    private static final String ADMIN_TOKEN = "admin-secret-token-1234";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        User fakeUser = User.builder()
            .provider("kakao")
            .providerId("dev")
            .lastLogin(LocalDateTime.now())
            .build();

        fakeUser.setId(1L);
        fakeUser.updateNickname("user");

        UserPrincipal principal = new UserPrincipal(fakeUser, Map.of());

        Admin fakeAdmin = Admin.builder()
            .id(1L)
            .username("admin")
            .password("admin")
            .lastLogin(LocalDateTime.now())
            .build();

        AdminPrincipal adminPrincipal = new AdminPrincipal(fakeAdmin);

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.equals("Bearer " + DEV_TOKEN)) {
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

            Authentication auth = new UsernamePasswordAuthenticationToken(principal, null,
                authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else if (authHeader != null && authHeader.equals("Bearer " + ADMIN_TOKEN)) {
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

            Authentication auth = new UsernamePasswordAuthenticationToken(adminPrincipal, null,
                authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
