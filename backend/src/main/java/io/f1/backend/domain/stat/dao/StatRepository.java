package io.f1.backend.domain.stat.dao;

import io.f1.backend.domain.stat.dto.StatWithNickname;
import io.f1.backend.domain.stat.entity.Stat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StatRepository extends JpaRepository<Stat, Long> {

    @Query(
            """
            SELECT
            		new io.f1.backend.domain.stat.dto.StatWithNickname
            				(u.nickname, s.totalGames, s.winningGames, s.score)
            FROM
            		Stat s JOIN s.user u
            """)
    Page<StatWithNickname> findWithUser(Pageable pageable);
}
