package com.example.weather.config;

import com.example.weather.client.AemetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Precarga el listado de municipios al arrancar para que ningún usuario pague la primera llamada lenta. */
@Component
class CacheWarmup implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmup.class);
    private final AemetClient aemetClient;

    CacheWarmup(AemetClient aemetClient) {
        this.aemetClient = aemetClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            int total = aemetClient.getMunicipios().size();
            log.info("Caché de municipios precargada ({} municipios)", total);
        } catch (Exception e) {
            log.warn("No se pudo precargar la caché de municipios: {}", e.getMessage());
        }
    }
}
