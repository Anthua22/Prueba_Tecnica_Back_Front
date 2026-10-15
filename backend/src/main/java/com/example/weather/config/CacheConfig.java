package com.example.weather.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cachés en memoria con TTL distinto según la volatilidad del dato.
 * Si el backend se escala a varias instancias, basta con cambiar este bean por un CacheManager de Redis.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String MUNICIPIOS = "municipios";
    public static final String PREDICCIONES = "predicciones";

    @Bean
    CacheManager cacheManager() {
        var manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                // El listado de municipios casi nunca cambia.
                new CaffeineCache(MUNICIPIOS, Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofHours(24))
                        .maximumSize(1)
                        .build()),
                // La predicción de AEMET se actualiza unas pocas veces al día.
                new CaffeineCache(PREDICCIONES, Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofHours(1))
                        .maximumSize(10_000)
                        .build())));
        return manager;
    }
}
