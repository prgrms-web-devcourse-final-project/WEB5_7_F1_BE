package io.f1.backend.domain.admin.entity;

import io.f1.backend.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Admin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDateTime lastLogin;

    public void updateLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Builder
    public Admin(Long id, String username, String password, LocalDateTime lastLogin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.lastLogin = LocalDateTime.now();
    }
}
