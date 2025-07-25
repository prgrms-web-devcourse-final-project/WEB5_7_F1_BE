package io.f1.backend.domain.stat.dao;

import io.f1.backend.domain.stat.dto.StatWithNickname;
import io.f1.backend.domain.stat.dto.StatWithNicknameAndUserId;
import io.f1.backend.domain.stat.entity.Stat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StatJpaRepository extends JpaRepository<Stat, Long> {

    @Query(
            """
            SELECT
            		new io.f1.backend.domain.stat.dto.StatWithNickname
            				(u.nickname, s.totalGames, s.winningGames, s.score)
            FROM
            		Stat s JOIN s.user u
            """)
    Page<StatWithNickname> findAllStatsWithUser(Pageable pageable);

    @Query("SELECT s.score FROM Stat s WHERE s.user.nickname = :nickname")
    Optional<Long> findScoreByNickname(String nickname);

    long countByScoreGreaterThan(Long score);

    @Query(
            """
            SELECT
            		new io.f1.backend.domain.stat.dto.StatWithNicknameAndUserId
            				(u.id, u.nickname, s.totalGames, s.winningGames, s.score)
            FROM
            		Stat s JOIN s.user u
            """)
    List<StatWithNicknameAndUserId> findAllStatWithNicknameAndUserId();

    @Modifying
    @Query(
            """
            UPDATE
            		Stat s
            SET
            		s.totalGames = s.totalGames + 1, s.winningGames = s.winningGames + 1, s.score = s.score + :deltaScore
            WHERE
            		s.user.id = :userId
            """)
    void updateStatByUserIdCaseWin(long deltaScore, long userId);

    @Modifying
    @Query(
            """
            UPDATE
            		Stat s
            SET
            		s.totalGames = s.totalGames + 1, s.score = s.score + :deltaScore
            WHERE
            		s.user.id = :userId
            """)
    void updateStatByUserIdCaseLose(long deltaScore, long userId);

    @Query(
            """
            SELECT new io.f1.backend.domain.stat.dto.StatWithNicknameAndUserId(
                u.id, u.nickname, s.totalGames, s.winningGames, s.score
            )
            FROM Stat s JOIN s.user u
            WHERE u.id = :userId
            """)
    Optional<StatWithNicknameAndUserId> findByUserId(long userId);
}
