package fr.insee.compas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("local")
public class LocalSecurityConfig {
    @SuppressWarnings({
        "java:S4502",
        "java:S2187"
    }) // CSRF is intentionally relaxed for stateless REST API, inutile de tester le framework sur
    // une classe locale.
    @Bean
    public SecurityFilterChain localSecurityFilterChain(HttpSecurity http) {

        http.authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/swagger-ui/**",
                                                "/v3/api-docs/**",
                                                "/swagger-ui.html",
                                                "/h2-console/**",
                                                "/webjars/**")
                                        .permitAll()
                                        .anyRequest()
                                        .permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .headers(
                        headers ->
                                headers.frameOptions(
                                        HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
