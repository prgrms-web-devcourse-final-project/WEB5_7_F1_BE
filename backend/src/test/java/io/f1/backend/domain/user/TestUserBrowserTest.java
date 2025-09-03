package io.f1.backend.domain.user;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.database.rider.core.api.dataset.DataSet;

import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.global.template.BrowserTestTemplate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.web.servlet.ResultActions;

public class TestUserBrowserTest extends BrowserTestTemplate {

    @Test
    @DataSet("datasets/user.yml")
    @DisplayName("테스트 유저가 로그인하면 세션에 SecurityContext가 저장된다")
    void testUserLogin() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();

        // when
        ResultActions result = mockMvc.perform(post("/user/test/login/1").session(session));

        // then
        result.andExpect(status().isOk());
        assertThat(session.getAttribute("SPRING_SECURITY_CONTEXT")).isNotNull();

        SecurityContext context = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
        assertThat(context.getAuthentication().getPrincipal()).isInstanceOf(UserPrincipal.class);

        UserPrincipal userPrincipal = (UserPrincipal) context.getAuthentication().getPrincipal();
        assertThat(userPrincipal.getUserId()).isEqualTo(1L);
        assertThat(userPrincipal.getUserNickname()).isEqualTo("USER1");
    }

    @Test
    @DataSet("datasets/stat/one-user-stat.yml")
    @DisplayName("테스트 유저가 로그인하면 마이페이지에 접근이 가능하다")
    void testUserLoginSecurityContext() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();

        // when
        ResultActions beforeLogin = mockMvc.perform(get("/user/me").session(session));
        mockMvc.perform(post("/user/test/login/1").session(session));
        ResultActions afterLogin = mockMvc.perform(get("/user/me").session(session));

        // then
        beforeLogin.andExpect(status().isUnauthorized());
        afterLogin.andExpect(status().isOk());
    }
}
