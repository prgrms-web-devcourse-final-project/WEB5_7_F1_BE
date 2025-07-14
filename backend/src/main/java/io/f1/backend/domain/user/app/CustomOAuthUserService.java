package io.f1.backend.domain.user.app;

import io.f1.backend.domain.stat.entity.Stat;
import io.f1.backend.domain.user.dao.UserRepository;
import io.f1.backend.domain.user.dto.SessionUser;
import io.f1.backend.domain.user.dto.UserPrincipal;
import io.f1.backend.domain.user.entity.User;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomOAuthUserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = Objects.requireNonNull(oAuth2User.getAttribute("id")).toString();

        User user =
                userRepository
                        .findByProviderAndProviderId(provider, providerId)
                        .map(this::updateLastLogin)
                        .orElseGet(() -> createNewUser(provider, providerId));

        httpSession.setAttribute("OAuthUser", new SessionUser(user));
        return new UserPrincipal(user, oAuth2User.getAttributes());
    }

    private User updateLastLogin(User user) {
        user.updateLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    private User createNewUser(String provider, String providerId) {
        User user =
                User.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .lastLogin(LocalDateTime.now())
                        .build();

        Stat stat = Stat.builder().totalGames(0L).winningGames(0L).score(0L).user(user).build();

        user.initStat(stat);
        return userRepository.save(user);
    }
}
