package io.f1.backend.domain.stat.app;

import io.f1.backend.domain.stat.dao.StatRepository;
import io.f1.backend.domain.stat.dto.StatPageResponse;

import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StatService {

    private final StatRepository statRepository;

    @Transactional(readOnly = true)
    public StatPageResponse getRanks(Pageable pageable, String nickname) {
		StatPageResponse response;

		if (StringUtils.isBlank(nickname)) {
			response = statRepository.getRanks(pageable);
		} else {
			response = statRepository.getRanksByNickname(nickname, pageable.getPageSize());
		}

		if (response.totalElements() == 0) {
			throw new CustomException(RoomErrorCode.PLAYER_NOT_FOUND);
		}

		return response;
    }

	// TODO: 게임 종료 후 호출 필요
	public void updateRank(long userId, boolean win, int deltaScore) {
		statRepository.updateRank(userId, win, deltaScore);
	}

	public void addUser(long userId, String nickname) {
		statRepository.addUser(userId, nickname);
	}

	public void removeUser(long userId) {
		statRepository.removeUser(userId);
	}

	public void updateNickname(long userId, String newNickname) {
		statRepository.updateNickname(userId, newNickname);
	}
}
