package io.f1.backend.domain.admin.app;

import io.f1.backend.domain.admin.dao.AdminRepository;
import io.f1.backend.domain.admin.dto.AdminPrincipal;
import io.f1.backend.domain.admin.entity.Admin;

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
                                () -> new UsernameNotFoundException("E404007: 존재하지 않는 관리자입니다."));
        // 프론트엔드로 내려가지 않는 예외
        return new AdminPrincipal(admin);
    }
}
