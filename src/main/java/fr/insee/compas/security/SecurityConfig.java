package fr.insee.compas.security;

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    @Profile("!local")
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
        http.csrf(csrf -> csrf.disable())
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

    @Bean
    @Profile("local")
    public SecurityFilterChain h2SecurityFilterChain(HttpSecurity http) {
        http.csrf(csrf -> csrf.disable())
                .securityMatcher(PathRequest.toH2Console())
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/v3/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/**",
                                                "/h2-console/**",
                                                "/webjars/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
                .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
