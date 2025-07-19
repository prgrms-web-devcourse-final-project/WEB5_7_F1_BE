package io.f1.backend.domain.stat.app;

import static io.f1.backend.domain.stat.mapper.StatMapper.toStatListPageResponse;

import io.f1.backend.domain.stat.dao.StatRepository;
import io.f1.backend.domain.stat.dto.StatPageResponse;
import io.f1.backend.domain.stat.dto.StatWithNickname;
import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatService {

    private final StatRepository statRepository;

    @Transactional(readOnly = true)
    public StatPageResponse getRanks(Pageable pageable) {
        Page<StatWithNickname> stats = statRepository.findWithUser(pageable);
        return toStatListPageResponse(stats);
    }

    @Transactional(readOnly = true)
    public StatPageResponse getRanksByNickname(String nickname, int pageSize) {

        Page<StatWithNickname> stats =
                statRepository.findWithUser(getPageableFromNickname(nickname, pageSize));

        return toStatListPageResponse(stats);
    }

    private Pageable getPageableFromNickname(String nickname, int pageSize) {
        long score =
                statRepository
                        .findScoreByNickname(nickname)
                        .orElseThrow(() -> new CustomException(RoomErrorCode.PLAYER_NOT_FOUND));

        long rowNum = statRepository.countByScoreGreaterThan(score);

        int pageNumber = rowNum > 0 ? (int) (rowNum / pageSize) : 0;
        return PageRequest.of(pageNumber, pageSize, Sort.by(Direction.DESC, "score"));
    }
}
