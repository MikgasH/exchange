package com.example.cerpshashkin.config;

import com.example.cerpshashkin.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ROLE_USER = "USER";
    private static final String ROLE_PREMIUM_USER = "PREMIUM_USER";
    private static final String ROLE_ADMIN = "ADMIN";

    // Currency API endpoints
    private static final String ENDPOINT_CONVERT = "/api/v1/currency/convert";
    private static final String ENDPOINT_RATES = "/api/v1/currency/rates";
    private static final String ENDPOINT_SUPPORTED = "/api/v1/currency/supported";

    // Публичные endpoints (Swagger и healthcheck)
    private static final String[] PUBLIC_ENDPOINTS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/**"
    };

    private static final int BCRYPT_STRENGTH = 12;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Swagger и Actuator публичные
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        // Currency API endpoints - требуют JWT + роли
                        .requestMatchers(HttpMethod.GET, ENDPOINT_CONVERT)
                        .hasAnyRole(ROLE_USER, ROLE_PREMIUM_USER, ROLE_ADMIN)

                        .requestMatchers(HttpMethod.GET, ENDPOINT_RATES)
                        .hasAnyRole(ROLE_USER, ROLE_PREMIUM_USER, ROLE_ADMIN)

                        .requestMatchers(HttpMethod.GET, ENDPOINT_SUPPORTED)
                        .hasAnyRole(ROLE_USER, ROLE_PREMIUM_USER, ROLE_ADMIN)

                        // Все остальные endpoints требуют аутентификации
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
