package fr.insee.compas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Value("${app.version}")
    private String version;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("Api tableaux de bord Compas").version(version));
    }
}
