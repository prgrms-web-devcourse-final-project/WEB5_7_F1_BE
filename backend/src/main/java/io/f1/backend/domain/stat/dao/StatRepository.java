package io.f1.backend.domain.stat.dao;

import io.f1.backend.domain.stat.dto.StatPageResponse;
import io.f1.backend.domain.user.dto.MyPage;

import org.springframework.data.domain.Pageable;

public interface StatRepository {

    StatPageResponse getRanks(Pageable pageable);

    StatPageResponse getRanksByNickname(String nickname, int pageSize);

    void addUser(long userId, String nickname);

    void updateRank(long userId, boolean win, int deltaScore);

    void updateNickname(long userId, String nickname);

    void removeUser(long userId);

    MyPage getMyPageByUserId(long userId);
}
