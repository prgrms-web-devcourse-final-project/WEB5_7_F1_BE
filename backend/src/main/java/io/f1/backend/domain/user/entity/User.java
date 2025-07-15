package io.f1.backend.domain.user.entity;

import io.f1.backend.domain.stat.entity.Stat;
import io.f1.backend.global.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter // quizService의 퀴즈 조회 메서드 구현 시까지 임시 사용
@Entity
@Table(name = "`user`")
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Stat stat;

    @Column(unique = true)
    private String nickname;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false)
    private LocalDateTime lastLogin;

    @Builder
    public User(String provider, String providerId, LocalDateTime lastLogin) {
        this.provider = provider;
        this.providerId = providerId;
        this.lastLogin = lastLogin;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void initStat(Stat stat) {
        this.stat = stat;
    }
}
