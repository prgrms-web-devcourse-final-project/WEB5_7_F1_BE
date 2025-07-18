package io.f1.backend.domain.stat.mapper;

import io.f1.backend.domain.stat.dto.StatWithNickname;
import io.f1.backend.domain.stat.dto.StatPageResponse;
import io.f1.backend.domain.stat.dto.StatResponse;
import org.springframework.data.domain.Page;

public class StatMapper {

	public static StatPageResponse toStatListPageResponse(Page<StatWithNickname> statPage) {
		int curPage = statPage.getNumber() + 1;

		return new StatPageResponse(
			statPage.getTotalPages(),
			curPage,
			statPage.getNumberOfElements(),
			statPage.stream().map(
				stat -> {
					long rank = getRankFromPage(
						curPage,
						statPage.getSize(),
						statPage.getContent().indexOf(stat)
					);
					return toStatResponse(stat, rank);
				}
			).toList()
		);
	}

	private static StatResponse toStatResponse(StatWithNickname stat, long rank) {
		return new StatResponse(
			rank,
			stat.nickname(),
			stat.totalGames(),
			stat.winningGames(),
			stat.score()
		);
	}

	private static long getRankFromPage(int curPage, int pageSize, int index) {
		int startRank = (curPage - 1) * pageSize + 1;
		return startRank + index;
	}
}
