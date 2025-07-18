package io.f1.backend.domain.admin.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.database.rider.core.api.dataset.DataSet;

import io.f1.backend.global.template.BrowserTestTemplate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

@WithMockUser(roles = "ADMIN")
public class AdminServiceTests extends BrowserTestTemplate {

    @Test
    @DataSet("datasets/admin/sorted-user.yml")
    @DisplayName("가입한 유저가 3명이면 첫 페이지에서 3개의 결과를 반환한다")
    void totalUser() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/admin/users"));

        // then
        result.andExpectAll(
                status().isOk(),
                jsonPath("$.totalPages").value(1),
                jsonPath("$.currentPage").value(1),
                jsonPath("$.totalElements").value(3),
                jsonPath("$.users.length()").value(3));
    }

    @Test
    @DataSet("datasets/admin/sorted-user.yml")
    @DisplayName("유저 목록이 id 순으로 정렬되어 반환된다")
    void getUsersSortedByLastLogin() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/admin/users"));
        // then
        result.andExpectAll(
                status().isOk(),
                jsonPath("$.totalElements").value(3),
                // 가장 최근 로그인한 USER3이 첫 번째
                jsonPath("$.users[0].id").value(1),
                // 중간 로그인한 USER2가 두 번째
                jsonPath("$.users[1].id").value(2),
                // 가장 오래된 로그인한 USER1이 세 번째
                jsonPath("$.users[2].id").value(3));
    }
}
