package io.f1.backend.global.config;

import io.f1.backend.domain.user.app.CustomOAuthUserService;
import io.f1.backend.domain.user.app.handler.CustomAuthenticationEntryPoint;
import io.f1.backend.domain.user.app.handler.OAuthLogoutSuccessHandler;
import io.f1.backend.domain.user.app.handler.OAuthSuccessHandler;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomOAuthUserService customOAuthUserService;
    private final OAuthSuccessHandler oAuthSuccessHandler;
    private final OAuthLogoutSuccessHandler oAuthLogoutSuccessHandler;

    @Bean
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(
                        exception ->
                                exception.authenticationEntryPoint(customAuthenticationEntryPoint))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/",
                                                "/login",
                                                "/oauth2/**",
                                                "/signup",
                                                "/css/**",
                                                "/js/**")
                                        .permitAll()
                                        .requestMatchers("/ws/**")
                                        .authenticated()
                                        .anyRequest()
                                        .authenticated())
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2Login(
                        oauth2 ->
                                oauth2.userInfoEndpoint(
                                                userInfo ->
                                                        userInfo.userService(
                                                                customOAuthUserService))
                                        .successHandler(oAuthSuccessHandler))
                .logout(
                        logout ->
                                logout.logoutUrl("/logout")
                                        .logoutSuccessHandler(oAuthLogoutSuccessHandler)
                                        .clearAuthentication(true)
                                        .invalidateHttpSession(true)
                                        .permitAll());
        return http.build();
    }
}
