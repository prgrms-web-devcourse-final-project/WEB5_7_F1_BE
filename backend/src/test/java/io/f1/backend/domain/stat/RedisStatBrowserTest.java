package io.f1.backend.domain.stat;

import static io.f1.backend.global.exception.errorcode.RoomErrorCode.PLAYER_NOT_FOUND;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static java.lang.Thread.sleep;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;

import io.f1.backend.domain.stat.dao.StatJpaRepository;
import io.f1.backend.domain.stat.dao.StatRepositoryAdapter;
import io.f1.backend.domain.stat.dto.StatWithNickname;
import io.f1.backend.domain.stat.dto.StatWithUserSummary;
import io.f1.backend.domain.user.dao.UserRepository;
import io.f1.backend.domain.user.dto.SignupRequest;
import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.config.RedisTestContainerConfig;
import io.f1.backend.global.template.BrowserTestTemplate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

@WithMockUser
@Import(RedisTestContainerConfig.class)
public class RedisStatBrowserTest extends BrowserTestTemplate {
    @Autowired ObjectMapper om;

    @Autowired UserRepository userRepository;

    @Autowired StatRepositoryAdapter statRepositoryAdapter;

    @Autowired RedisConnectionFactory redisConnectionFactory;

    @MockitoBean StatJpaRepository statJpaRepository; // JPA 미사용

    @BeforeEach
    public void setup() {
        redisConnectionFactory.getConnection().serverCommands().flushAll();
    }

    @Test
    @DataSet("datasets/stat/one-user-stat.yml")
    @DisplayName("새로운 유저가 회원가입하면 Redis에 반영되어 JPA 없이 랭킹 조회가 가능하다")
    void totalRankingNewUserWithoutJpa() throws Exception {
        // given
        User user = userRepository.findById(1L).orElseThrow(AssertionError::new);
        MockHttpSession session = getMockSession(user, false);

        String nickname = "TEST";
        SignupRequest signupRequest = new SignupRequest(nickname);

        // when
        mockMvc.perform(
                        post("/signup")
                                .session(session)
                                .header("content-type", "application/json")
                                .content(om.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        sleep(1000);

        ResultActions result = mockMvc.perform(get("/stats/rankings").param("nickname", nickname));

        // then
        checkExpectedRankingOneUser(result, nickname);
        verifyNeverUsedJpa();
    }

    @Test
    @DataSet("datasets/stat/one-user-stat.yml")
    @DisplayName("기존 유저가 닉네임을 변경하면 Redis에 반영되어 JPA 없이 랭킹 조회가 가능하다")
    void totalRankingChangeNicknameWithoutJpa() throws Exception {
        // given
        User user = userRepository.findById(1L).orElseThrow(AssertionError::new);
        MockHttpSession session = getMockSession(user, true);

        warmingRedisOneUser(user);

        String nickname = "TEST";
        SignupRequest signupRequest = new SignupRequest(nickname);

        // when
        mockMvc.perform(
                        put("/user/me")
                                .session(session)
                                .header("content-type", "application/json")
                                .content(om.writeValueAsString(signupRequest)))
                .andExpect(status().isNoContent());

        sleep(1000);

        ResultActions result = mockMvc.perform(get("/stats/rankings").param("nickname", nickname));

        // then
        checkExpectedRankingOneUser(result, nickname);
        verifyNeverUsedJpa();
    }

    @Test
    @DataSet("datasets/stat/one-user-stat.yml")
    @DisplayName("기존 유저가 삭제되면 Redis 조회 결과가 없어 JPARepository로 fallback 된다")
    void totalRankingDeleteUserFallbackToJpa() throws Exception {
        // given
        User user = userRepository.findById(1L).orElseThrow(AssertionError::new);
        MockHttpSession session = getMockSession(user, true);

        Pageable pageable = PageRequest.of(0, 10, Direction.DESC, "score");
        Page<StatWithNickname> expectedPage = new PageImpl<>(List.of(), pageable, 0);
        given(statJpaRepository.findAllStatsWithUser(any())).willReturn(expectedPage);

        warmingRedisOneUser(user);

        // when
        mockMvc.perform(delete("/user/me").session(session)).andExpect(status().isNoContent());

        sleep(1000);

        ResultActions result = mockMvc.perform(get("/stats/rankings"));

        // then
        verify(statJpaRepository, times(1)).findAllStatsWithUser(any());
        result.andExpectAll(
                status().isNotFound(), jsonPath("$.code").value(PLAYER_NOT_FOUND.getCode()));
    }

    private void checkExpectedRankingOneUser(ResultActions result, String nickname)
            throws Exception {
        result.andExpectAll(
                status().isOk(),
                jsonPath("$.totalPages").value(1),
                jsonPath("$.currentPage").value(1),
                jsonPath("$.totalElements").value(1),
                jsonPath("$.ranks.length()").value(1),
                jsonPath("$.ranks[0].nickname").value(nickname));
    }

    private void verifyNeverUsedJpa() {
        verify(statJpaRepository, never()).findScoreByNickname(anyString());
        verify(statJpaRepository, never()).countByScoreGreaterThan(anyLong());
        verify(statJpaRepository, never()).findAllStatsWithUser(any());
    }

    private void warmingRedisOneUser(User user) {
        StatWithUserSummary mockStat =
                new StatWithUserSummary(user.getId(), user.getNickname(), 10, 10, 100);
        given(statJpaRepository.findAllStatWithUserSummary()).willReturn(List.of(mockStat));
        statRepositoryAdapter.setup();
    }
}
