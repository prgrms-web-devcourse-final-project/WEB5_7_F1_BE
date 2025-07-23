package io.f1.backend.domain.stat.dao;

import static io.f1.backend.domain.stat.mapper.StatMapper.toStatListPageResponse;

import io.f1.backend.domain.stat.dto.StatPageResponse;
import io.f1.backend.domain.stat.dto.StatWithNickname;
import io.f1.backend.domain.stat.dto.StatWithNicknameAndUserId;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StatRepositoryAdapter implements StatRepository {

	private final StatJpaRepository jpaRepository;
	private final StatRedisRepository redisRepository;

	@PostConstruct
	public void setup() {
		redisRepository.setup();
		warmingRedis();
	}

	@Override
	public StatPageResponse getRanks(Pageable pageable) {
		try {
			return redisRepository.findAllStatsWithUser(pageable);
		} catch (NullPointerException e) {
			log.error("Cache miss fall back to MySQL", e);
		}

		Page<StatWithNickname> stats = jpaRepository.findAllStatsWithUser(pageable);
		return toStatListPageResponse(stats);
	}

	@Override
	public StatPageResponse getRanksByNickname(String nickname, int pageSize) {
		Pageable pageable = getPageableFromNickname(nickname, pageSize);
		return getRanks(pageable);
	}

	@Override
	public void addUser(long userId, String nickname) {
		redisRepository.initialize(new StatWithNicknameAndUserId(
			userId, nickname, 0, 0, 0
		));
	}

	@Override
	public void updateRank(long userId, boolean win, int deltaScore) {
		redisRepository.updateRank(userId, win, deltaScore);
		if (win) {
			jpaRepository.updateStatByUserIdCaseWin(deltaScore, userId);
		} else {
			jpaRepository.updateStatByUserIdCaseLose(deltaScore, userId);
		}
	}

	@Override
	public void updateNickname(long userId, String nickname) {
		redisRepository.updateNickname(userId, nickname);
	}

	@Override
	public void removeUser(long userId) {
		redisRepository.removeUser(userId);
	}

	private void warmingRedis() {
		jpaRepository.findAllStatWithNicknameAndUserId().forEach(redisRepository::initialize);
	}

	private Pageable getPageableFromNickname(String nickname, int pageSize) {
		try {
			return redisRepository.getPageableFromNickname(nickname, pageSize);
		}  catch (NullPointerException e) {
			log.error("Cache miss fall back to MySQL", e);
		}
		long score =
			jpaRepository
				.findScoreByNickname(nickname)
				.orElseThrow(() -> new CustomException(RoomErrorCode.PLAYER_NOT_FOUND));

		long rowNum = jpaRepository.countByScoreGreaterThan(score);

		int pageNumber = rowNum > 0 ? (int) (rowNum / pageSize) : 0;
		return PageRequest.of(pageNumber, pageSize, Sort.by(Direction.DESC, "score"));
	}
}
