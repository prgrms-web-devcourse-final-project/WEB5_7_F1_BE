package io.f1.backend.domain.user.dao;

import io.f1.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

// TODO : 퀴즈 생성을 위한 user 생성을 위해 임의로 만듦.
public interface UserRepository extends JpaRepository<User, Long> {

}
