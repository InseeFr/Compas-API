package fr.insee.compas.client.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

@Configuration
public class HyperXFeignConfig {
    @Bean
    public RequestInterceptor hyperXAuthInterceptor(
            @Value("${fr.insee.compas.key.hyperx}") String token) {
        return requestTemplate -> requestTemplate.header("x-api-key", token);
    }
}
