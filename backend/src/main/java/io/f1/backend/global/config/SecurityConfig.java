package io.f1.backend.global.config;

import io.f1.backend.domain.admin.app.handler.AdminLoginFailureHandler;
import io.f1.backend.domain.admin.app.handler.AdminLoginSuccessHandler;
import io.f1.backend.domain.user.app.CustomOAuthUserService;
import io.f1.backend.domain.user.app.handler.CustomAuthenticationEntryPoint;
import io.f1.backend.domain.user.app.handler.OAuthSuccessHandler;
import io.f1.backend.domain.user.app.handler.UserAndAdminLogoutSuccessHandler;
import io.f1.backend.global.filter.DevTokenAuthFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomOAuthUserService customOAuthUserService;
    private final OAuthSuccessHandler oAuthSuccessHandler;
    private final UserAndAdminLogoutSuccessHandler userAndAdminLogoutSuccessHandler;
    private final AdminLoginSuccessHandler adminLoginSuccessHandler;
    private final AdminLoginFailureHandler adminLoginFailureHandler;

    @Value("${management.endpoints.web.base-path:/actuator}")
    private String actuatorBasePath;

    @Bean
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .addFilterBefore(
                        new DevTokenAuthFilter(), UsernamePasswordAuthenticationFilter.class)
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
                                                "/js/**",
                                                "/admin/login")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        .requestMatchers("/ws/**")
                                        .authenticated()
                                        .requestMatchers("/user/me")
                                        .hasRole("USER")
                                        .requestMatchers("/admin/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers("/auth/me")
                                        .hasAnyRole("USER", "ADMIN")
                                        .requestMatchers("/quizzes/**")
                                        .hasAnyRole("USER", "ADMIN")
                                        .requestMatchers("/questions/**")
                                        .hasAnyRole("USER", "ADMIN")
                                        .requestMatchers(actuatorBasePath + "/**")
                                        .hasRole("PROMETHEUS")
                                        .anyRequest()
                                        .authenticated())
                .formLogin(
                        form ->
                                form.loginProcessingUrl("/admin/login") // 로그인 form action 경로
                                        .successHandler(adminLoginSuccessHandler)
                                        .failureHandler(adminLoginFailureHandler)
                                        .permitAll())
                .oauth2Login(
                        oauth2 ->
                                oauth2.userInfoEndpoint(
                                                userInfo ->
                                                        userInfo.userService(
                                                                customOAuthUserService))
                                        .successHandler(oAuthSuccessHandler))
				.httpBasic(Customizer.withDefaults())
                .logout(
                        logout ->
                                logout.logoutUrl("/logout")
                                        .logoutSuccessHandler(userAndAdminLogoutSuccessHandler)
                                        .clearAuthentication(true)
                                        .invalidateHttpSession(true)
                                        .permitAll())
                .sessionManagement(
                        session -> session.sessionFixation().migrateSession().maximumSessions(1));
        return http.build();
    }
}
