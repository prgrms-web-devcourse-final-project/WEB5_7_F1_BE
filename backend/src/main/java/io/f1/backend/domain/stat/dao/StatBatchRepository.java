package io.f1.backend.domain.stat.dao;

import io.f1.backend.domain.stat.dto.StatChangeEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StatBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchUpdateStats(List<StatChangeEvent> events) {
        if (events.isEmpty()) {
            return;
        }

        List<StatChangeEvent> winEvents = events.stream().filter(StatChangeEvent::isWin).toList();

        List<StatChangeEvent> loseEvents = events.stream().filter(e -> !e.isWin()).toList();

        if (!winEvents.isEmpty()) {
            batchUpdateWinStats(winEvents);
        }

        if (!loseEvents.isEmpty()) {
            batchUpdateLoseStats(loseEvents);
        }
    }

    private void batchUpdateWinStats(List<StatChangeEvent> events) {
        StringBuilder sql =
                new StringBuilder(
                        """
                        UPDATE stat SET
                            total_games = total_games + 1,
                            winning_games = winning_games + 1,
                            score = score + CASE user_id
                        """);

        for (StatChangeEvent event : events) {
            sql.append(String.format("WHEN %d THEN %d ", event.getUserId(), event.getDeltaScore()));
        }

        sql.append("END WHERE user_id IN (");
        sql.append(
                events.stream()
                        .map(e -> String.valueOf(e.getUserId()))
                        .collect(Collectors.joining(",")));
        sql.append(")");

        int updatedRows = jdbcTemplate.update(sql.toString());
        log.debug("Batch updated {} win stats", updatedRows);
    }

    private void batchUpdateLoseStats(List<StatChangeEvent> events) {
        StringBuilder sql =
                new StringBuilder(
                        """
                        UPDATE stat SET
                            total_games = total_games + 1,
                            score = score + CASE user_id
                        """);

        for (StatChangeEvent event : events) {
            sql.append(String.format("WHEN %d THEN %d ", event.getUserId(), event.getDeltaScore()));
        }

        sql.append("END WHERE user_id IN (");
        sql.append(
                events.stream()
                        .map(e -> String.valueOf(e.getUserId()))
                        .collect(Collectors.joining(",")));
        sql.append(")");

        int updatedRows = jdbcTemplate.update(sql.toString());
        log.debug("Batch updated {} lose stats", updatedRows);
    }
}
