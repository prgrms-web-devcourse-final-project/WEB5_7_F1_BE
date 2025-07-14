package io.f1.backend.domain.stat.entity;

import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long totalGames;

    @Column(nullable = false)
    private Long winningGames;

    @Column(nullable = false)
    private Long score;

    @Builder
    public Stat(User user, Long totalGames, Long winningGames, Long score) {
        this.user = user;
        this.totalGames = totalGames;
        this.winningGames = winningGames;
        this.score = score;
    }
}
