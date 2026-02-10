package fr.insee.compas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("!local")
public class SecurityConfig {
    @SuppressWarnings("java:S4502") // CSRF is intentionally relaxed for stateless REST API
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
        http.csrf(
                        csrf ->
                                csrf.ignoringRequestMatchers(
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/kpi-green/**",
                                        "/qualite/**",
                                        "/securite/**",
                                        "/devops/**",
                                        "/module-oscar/**",
                                        "/meteo/**",
                                        "/a11y/**",
                                        "/cloud/**"))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/v3/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/**",
                                                "/webjars/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
                .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
