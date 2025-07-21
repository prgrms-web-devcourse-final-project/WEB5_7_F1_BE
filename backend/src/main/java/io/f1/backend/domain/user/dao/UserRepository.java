package io.f1.backend.domain.user.dao;

import io.f1.backend.domain.admin.dto.UserResponse;
import io.f1.backend.domain.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    Boolean existsUserByNicknameIgnoreCase(String nickname);

    @Query(
            "SELECT new io.f1.backend.domain.admin.dto.UserResponse(u.id, u.nickname, u.lastLogin,"
                    + " u.createdAt)FROM User u ORDER BY u.id")
    Page<UserResponse> findAllUsersWithPaging(Pageable pageable);

    @Query(
            "SELECT new io.f1.backend.domain.admin.dto.UserResponse(u.id, u.nickname, u.lastLogin,"
                + " u.createdAt) FROM User u WHERE LOWER(u.nickname) LIKE CONCAT('%',"
                + " LOWER(:nickname), '%')")
    Page<UserResponse> findUsersByNicknameContaining(String nickname, Pageable pageable);
}
