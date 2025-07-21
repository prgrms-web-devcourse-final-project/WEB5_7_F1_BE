package io.f1.backend.domain.admin.app;

import static io.f1.backend.domain.admin.mapper.AdminMapper.toUserListPageResponse;

import io.f1.backend.domain.admin.dto.UserPageResponse;
import io.f1.backend.domain.admin.dto.UserResponse;
import io.f1.backend.domain.user.dao.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserPageResponse getAllUsers(Pageable pageable) {
        Page<UserResponse> users = userRepository.findAllUsersWithPaging(pageable);
        return toUserListPageResponse(users);
    }

    @Transactional(readOnly = true)
    public UserPageResponse searchUsersByNickname(String nickname, Pageable pageable) {
        Page<UserResponse> users = userRepository.findUsersByNicknameContaining(nickname, pageable);
        return toUserListPageResponse(users);
    }
}
