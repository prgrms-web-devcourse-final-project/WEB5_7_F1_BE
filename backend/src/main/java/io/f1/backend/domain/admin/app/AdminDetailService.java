package io.f1.backend.domain.admin.app;

import io.f1.backend.domain.admin.dao.AdminRepository;
import io.f1.backend.domain.admin.dto.AdminPrincipal;
import io.f1.backend.domain.admin.entity.Admin;
import io.f1.backend.global.exception.errorcode.AdminErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDetailService implements UserDetailsService {

    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin =
                adminRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                AdminErrorCode.ADMIN_NOT_FOUND.getMessage()));
        return new AdminPrincipal(admin);
    }
}
