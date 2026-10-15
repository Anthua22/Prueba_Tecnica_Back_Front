package com.example.weather.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Weather API")
                .version("1.0.0")
                .description("Búsqueda de municipios y previsión del día siguiente, sobre la API de AEMET."));
    }
}
