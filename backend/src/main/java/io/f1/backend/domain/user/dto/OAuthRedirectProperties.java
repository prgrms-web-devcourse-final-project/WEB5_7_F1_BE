package io.f1.backend.domain.user.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom.oauth.redirect")
public record OAuthRedirectProperties(
    String signupUrl,
    String mainUrl
) {

}

