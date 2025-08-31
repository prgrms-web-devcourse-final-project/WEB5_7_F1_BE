package io.f1.backend.global.template;

import static io.f1.backend.domain.user.constants.SessionKeys.OAUTH_USER;
import static io.f1.backend.domain.user.constants.SessionKeys.USER;

import com.github.database.rider.spring.api.DBRider;

import io.f1.backend.domain.user.dto.AuthenticationUser;
import io.f1.backend.domain.user.entity.User;
import io.f1.backend.global.security.util.SecurityUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@DBRider
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BrowserTestTemplate {
    @Autowired protected MockMvc mockMvc;

    protected MockHttpSession getMockSession(User user, boolean signup) {
        MockHttpSession session = new MockHttpSession();
        if (signup) {
            session.setAttribute(USER, AuthenticationUser.from(user));
            SecurityUtils.setAuthentication(user);
        } else {
            session.setAttribute(OAUTH_USER, AuthenticationUser.from(user));
        }

        return session;
    }
}
