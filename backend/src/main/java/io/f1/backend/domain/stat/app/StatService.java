package io.f1.backend.domain.stat.app;

import static io.f1.backend.domain.stat.mapper.StatMapper.toStatListPageResponse;

import io.f1.backend.domain.stat.dto.StatWithNickname;
import io.f1.backend.domain.stat.dao.StatRepository;
import io.f1.backend.domain.stat.dto.StatPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatService {

	private final StatRepository statRepository;

	public StatPageResponse getRanks(Pageable pageable) {
		Page<StatWithNickname> stats = statRepository.findWithUser(pageable);
		return toStatListPageResponse(stats);
	}
}
