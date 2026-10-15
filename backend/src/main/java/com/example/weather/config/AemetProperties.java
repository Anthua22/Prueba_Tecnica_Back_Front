package com.example.weather.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Configuración de acceso a la API de AEMET. Falla al arrancar si falta la API key. */
@Validated
@ConfigurationProperties(prefix = "aemet")
public record AemetProperties(
        @NotBlank String baseUrl,
        @NotBlank(message = "Falta la variable de entorno AEMET_API_KEY") String apiKey) {
}
