package io.f1.backend.domain.stat;

import static io.f1.backend.global.exception.errorcode.CommonErrorCode.INVALID_PAGINATION;

import static io.f1.backend.global.exception.errorcode.RoomErrorCode.PLAYER_NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.database.rider.core.api.dataset.DataSet;

import io.f1.backend.global.exception.errorcode.ErrorCode;
import io.f1.backend.global.exception.errorcode.RoomErrorCode;
import io.f1.backend.global.template.BrowserTestTemplate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

@WithMockUser
public class StatBrowserTest extends BrowserTestTemplate {
    @Test
    @DataSet("datasets/stat/one-user-stat.yml")
    @DisplayName("총 유저 수가 1명이면 첫 페이지에서 1개의 결과를 반환한다")
    void totalRankingForSingleUser() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/stats/rankings"));

        // then
        result.andExpectAll(
                status().isOk(),
                jsonPath("$.totalPages").value(1),
                jsonPath("$.currentPage").value(1),
                jsonPath("$.totalElements").value(1),
                jsonPath("$.ranks.length()").value(1));
    }

    @Test
    @DisplayName("100을 넘는 페이지 크기 요청이 오면 예외를 발생시킨다")
    void totalRankingWithInvalidPageSize() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/stats/rankings").param("size", "101"));

        // then
        result.andExpectAll(
                status().isBadRequest(), jsonPath("$.code").value(INVALID_PAGINATION.getCode()));
    }

    @Test
    @DataSet("datasets/stat/three-user-stat.yml")
    @DisplayName("총 유저 수가 3명이면 첫 페이지에서 3개의 결과를 반환한다")
    void totalRankingForThreeUser() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/stats/rankings"));

        // then
        result.andExpectAll(
                status().isOk(),
                jsonPath("$.totalPages").value(1),
                jsonPath("$.currentPage").value(1),
                jsonPath("$.totalElements").value(3),
                jsonPath("$.ranks.length()").value(3));
    }

    @Test
    @DataSet("datasets/stat/three-user-stat.yml")
    @DisplayName("총 유저 수가 3명일 때 페이지 크기가 2이면, 첫 페이지에서 2개, 두 번째 페이지에 1개의 결과를 반환한다")
    void totalRankingForThreeUserWithPageSize2() throws Exception {
        // when
        ResultActions resultPage1 =
                mockMvc.perform(get("/stats/rankings").param("page", "1").param("size", "2"));

        ResultActions resultPage2 =
                mockMvc.perform(get("/stats/rankings").param("page", "2").param("size", "2"));

        // then
        resultPage1.andExpectAll(
                status().isOk(),
                jsonPath("$.totalPages").value(2),
                jsonPath("$.currentPage").value(1),
                jsonPath("$.totalElements").value(2),
                jsonPath("$.ranks.length()").value(2));

        resultPage2.andExpectAll(
                status().isOk(),
                jsonPath("$.totalPages").value(2),
                jsonPath("$.currentPage").value(2),
                jsonPath("$.totalElements").value(1),
                jsonPath("$.ranks.length()").value(1));
    }

	@Test
	@DataSet("datasets/stat/three-user-stat.yml")
	@DisplayName("랭킹 페이지에서 존재하지 않는 닉네임을 검색하면 예외를 발생시킨다.")
	void totalRankingWithUnregisteredNickname() throws Exception {
		// given
		String nickname = "UNREGISTERED";

		// when
		ResultActions result = mockMvc.perform(get("/stats/rankings/" + nickname));

		// then
		result.andExpectAll(
			status().isNotFound(), jsonPath("$.code").value(PLAYER_NOT_FOUND.getCode()));
	}

	@Test
	@DataSet("datasets/stat/three-user-stat.yml")
	@DisplayName("총 유저 수가 3명이고 페이지 크기가 2일 때 1위 유저의 닉네임을 검색하면 첫 번째 페이지에 2개의 결과를 반환한다")
	void totalRankingForThreeUserWithFirstRankedNickname() throws Exception {
		// given
		String nickname = "USER3";

		// when
		ResultActions result = mockMvc.perform(
			get("/stats/rankings/" + nickname).param("size", "2"));

		// then
		result.andExpectAll(
			status().isOk(),
			jsonPath("$.totalPages").value(2),
			jsonPath("$.currentPage").value(1),
			jsonPath("$.totalElements").value(2),
			jsonPath("$.ranks.length()").value(2),
			jsonPath("$.ranks[0].nickname").value(nickname));
	}

	@Test
	@DataSet("datasets/stat/three-user-stat.yml")
	@DisplayName("총 유저 수가 3명이고 페이지 크기가 2일 때 3위 유저의 닉네임을 검색하면 두 번째 페이지에 1개의 결과를 반환한다")
	void totalRankingForThreeUserWithLastRankedNickname() throws Exception {
		// given
		String nickname = "USER1";

		// when
		ResultActions result = mockMvc.perform(
			get("/stats/rankings/" + nickname).param("size", "2"));

		// then
		result.andExpectAll(
			status().isOk(),
			jsonPath("$.totalPages").value(2),
			jsonPath("$.currentPage").value(2),
			jsonPath("$.totalElements").value(1),
			jsonPath("$.ranks.length()").value(1),
			jsonPath("$.ranks[0].nickname").value(nickname));
	}
}
