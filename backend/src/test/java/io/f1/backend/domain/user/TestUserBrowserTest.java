package io.f1.backend.domain.user;

import static io.f1.backend.domain.user.constants.SessionKeys.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;
import com.github.database.rider.core.api.dataset.DataSet;

import io.f1.backend.domain.user.dto.AuthenticationUser;
import io.f1.backend.global.template.BrowserTestTemplate;

public class TestUserBrowserTest extends BrowserTestTemplate {

    @Test
    @DataSet("datasets/user.yml")
    @DisplayName("테스트 유저가 로그인하면 세션에 유저 정보가 저장된다")
    void testUserLogin() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        
        // when
        ResultActions result = mockMvc.perform(post("/user/test/login/1").session(session));

        // then
        result.andExpect(status().isOk());
        assertThat(session.getAttribute(USER)).isNotNull();

        AuthenticationUser authenticationUser = (AuthenticationUser) session.getAttribute(USER);
        assertThat(authenticationUser.userId()).isEqualTo(1L);
        assertThat(authenticationUser.nickname()).isEqualTo("USER1");
        assertThat(authenticationUser.providerId()).isEqualTo("kakao1");
    }
}
