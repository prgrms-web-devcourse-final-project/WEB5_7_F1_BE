package io.f1.backend.domain.stat.dao;

import static java.lang.Long.parseLong;
import static java.util.Objects.requireNonNull;

import io.f1.backend.domain.stat.dto.StatPageResponse;
import io.f1.backend.domain.stat.dto.StatResponse;
import io.f1.backend.domain.stat.dto.StatWithUserSummary;
import io.f1.backend.domain.user.dto.MyPageInfo;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@RequiredArgsConstructor
public class StatRedisRepository {

    private static final String STAT_RANK = "stat:rank";
    private static final String STAT_USER = "stat:user:%d";
    private static final String STAT_NICKNAME = "stat:%s";

    private final RedisTemplate<String, Object> redisTemplate;
    private ZSetOperations<String, Object> zSetOps;
    private HashOperations<String, Object, Object> hashOps;
    private ValueOperations<String, Object> valueOps;

    public void setup() {
        zSetOps = redisTemplate.opsForZSet();
        hashOps = redisTemplate.opsForHash();
        valueOps = redisTemplate.opsForValue();
    }

    public void initialize(StatWithUserSummary stat) {
        String statUserKey = getStatUserKey(stat.userId());
        String statNicknameKey = getStatNickname(stat.nickname());

        // stat:user:{id}
        hashOps.put(statUserKey, "nickname", stat.nickname());
        hashOps.put(statUserKey, "totalGames", String.valueOf(stat.totalGames()));
        hashOps.put(statUserKey, "winningGames", String.valueOf(stat.winningGames()));

        // stat:rank
        zSetOps.add(STAT_RANK, stat.userId(), stat.score());

        // stat:{nickname}
        valueOps.set(statNicknameKey, stat.userId());
    }

    public void removeUser(long userId) {
        String statUserKey = getStatUserKey(userId);
        String nickname = (String) hashOps.get(statUserKey, "nickname");
        redisTemplate.delete(statUserKey);
        valueOps.getAndDelete(getStatNickname(nickname));
        zSetOps.remove(STAT_RANK, userId);
    }

    public StatPageResponse findAllStatsWithUser(Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        long startIdx = (long) page * size;

        long totalCount = getTotalMemberCount();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        Set<TypedTuple<Object>> rankSet = getRankSet(startIdx, size);

        AtomicInteger rankCounter = new AtomicInteger((int) startIdx + 1);
        List<StatResponse> ranks =
                rankSet.stream()
                        .map(rank -> convertToStatResponse(rank, rankCounter.getAndIncrement()))
                        .toList();

        return new StatPageResponse(totalPages, page + 1, rankSet.size(), ranks);
    }

    public Pageable getPageableFromNickname(String nickname, int pageSize) {
        long userId = getUserIdFromNickname(nickname);
        long rowNum = requireNonNull(zSetOps.reverseRank(STAT_RANK, userId)) + 1;
        int pageNumber = rowNum > 0 ? (int) (rowNum / pageSize) : 0;
        return PageRequest.of(pageNumber, pageSize, Sort.by(Direction.DESC, "score"));
    }

    private long getTotalMemberCount() {
        long count = requireNonNull(zSetOps.zCard(STAT_RANK));
        if (count == 0) {
            throw new NullPointerException("No member found in redis");
        }
        return count;
    }

    private Set<TypedTuple<Object>> getRankSet(long startIdx, int size) {
        Set<TypedTuple<Object>> result =
                zSetOps.reverseRangeWithScores(STAT_RANK, startIdx, startIdx + size - 1);
        return requireNonNull(result);
    }

    public void updateRank(long userId, boolean win, int deltaScore) {
        String statUserKey = getStatUserKey(userId);

        zSetOps.incrementScore(STAT_RANK, userId, deltaScore);
        hashOps.increment(statUserKey, "totalGames", 1);
        if (win) {
            hashOps.increment(statUserKey, "winningGames", 1);
        }
    }

    public void updateNickname(long userId, String newNickname) {
        String statUserKey = getStatUserKey(userId);
        valueOps.set(getStatNickname(newNickname), userId);

        String oldNickname = (String) hashOps.get(statUserKey, "nickname");
        valueOps.getAndDelete(getStatNickname(oldNickname));

        hashOps.put(statUserKey, "nickname", newNickname);
    }

    private StatResponse convertToStatResponse(TypedTuple<Object> rank, int rankValue) {
        long userId = ((Number) requireNonNull(rank.getValue())).longValue();
        Map<Object, Object> statUserMap = hashOps.entries(getStatUserKey(userId));

        return new StatResponse(
                rankValue,
                (String) statUserMap.get("nickname"),
                parseLong((String) statUserMap.get("totalGames")),
                parseLong((String) statUserMap.get("winningGames")),
                requireNonNull(rank.getScore()).longValue());
    }

    private static String getStatUserKey(long userId) {
        return String.format(STAT_USER, userId);
    }

    private static String getStatNickname(String nickname) {
        return String.format(STAT_NICKNAME, nickname);
    }

    private long getUserIdFromNickname(String nickname) {
        return ((Number) requireNonNull(valueOps.get(getStatNickname(nickname)))).longValue();
    }

    public MyPageInfo getStatByUserId(long userId) {
        String statUserKey = getStatUserKey(userId);

        Long rank = zSetOps.reverseRank(STAT_RANK, userId);
        Double score = zSetOps.score(STAT_RANK, userId);
        Map<Object, Object> statMap = hashOps.entries(statUserKey);

        if (rank == null || score == null || statMap.isEmpty()) {
            throw new IllegalStateException("User not found in Redis: " + userId);
        }

        return new MyPageInfo(
                (String) statMap.get("nickname"),
                rank + 1,
                score.longValue(),
                parseLong((String) statMap.get("totalGames")),
                parseLong((String) statMap.get("winningGames")));
    }
}
