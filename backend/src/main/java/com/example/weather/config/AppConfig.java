package com.example.weather.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AemetProperties.class)
public class AppConfig {

    /** Reloj inyectable: permite fijar la fecha en los tests. */
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Europe/Madrid"));
    }
}
