package com.example.weather.client;

import com.example.weather.client.dto.AemetEnvelope;
import com.example.weather.client.dto.AemetMunicipio;
import com.example.weather.client.dto.AemetPrediccionDiaria;
import com.example.weather.config.AemetProperties;
import com.example.weather.config.CacheConfig;
import com.example.weather.exception.AemetException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Único punto de contacto con AEMET. Aquí vive el protocolo de AEMET (API key, respuesta en dos pasos,
 * errores) y la caché, de forma que el resto de la aplicación no sabe que AEMET existe.
 *
 * {@code sync = true} hace que, ante varias peticiones concurrentes con la caché vacía, solo una llame a AEMET.
 */
@Component
public class AemetClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public AemetClient(RestClient.Builder builder, AemetProperties properties, ObjectMapper objectMapper) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        this.baseUrl = properties.baseUrl();
        this.objectMapper = objectMapper;
        this.restClient = builder
                .requestFactory(requestFactory)
                .defaultHeader("api_key", properties.apiKey())
                .build();
    }

    @Cacheable(cacheNames = CacheConfig.MUNICIPIOS, sync = true)
    public List<AemetMunicipio> getMunicipios() {
        return fetchViaEnvelope("/maestro/municipios", new TypeReference<List<AemetMunicipio>>() { });
    }

    @Cacheable(cacheNames = CacheConfig.PREDICCIONES, key = "#codigo", sync = true)
    public List<AemetPrediccionDiaria.Dia> getDailyForecast(String codigo) {
        List<AemetPrediccionDiaria> data = fetchViaEnvelope(
                "/prediccion/especifica/municipio/diaria/" + codigo,
                new TypeReference<List<AemetPrediccionDiaria>>() { });
        return data.stream()
                .findFirst()
                .map(AemetPrediccionDiaria::prediccion)
                .map(AemetPrediccionDiaria.Prediccion::dia)
                .orElseThrow(() -> new AemetException(HttpStatus.BAD_GATEWAY, "AEMET devolvió una predicción vacía"));
    }

    private <T> T fetch(URI uri, TypeReference<T> type) {
        try {
            ResponseEntity<byte[]> response = restClient.get().uri(uri).retrieve().toEntity(byte[].class);
            // AEMET a veces sirve el JSON como text/plain o con charset ISO-8859-1: decodificamos a mano.
            Charset charset = Optional.ofNullable(response.getHeaders().getContentType())
                    .map(MediaType::getCharset)
                    .orElse(StandardCharsets.UTF_8);
            byte[] body = Optional.ofNullable(response.getBody()).orElse(new byte[0]);
            return objectMapper.readValue(new String(body, charset), type);
        } catch (RestClientResponseException e) {
            throw translate(e);
        } catch (RestClientException | JsonProcessingException e) {
            throw new AemetException(HttpStatus.BAD_GATEWAY, "No se pudo obtener la información de AEMET", e);
        }
    }

    
    /** Flujo de AEMET en dos pasos: 1) respuesta con la URL "datos"; 2) descarga de los datos reales. */
    private <T> T fetchViaEnvelope(String path, TypeReference<T> type) {
        AemetEnvelope envelope = fetch(URI.create(baseUrl + path), new TypeReference<AemetEnvelope>() { });
        if (envelope.datos() == null || envelope.datos().isBlank()) {
            throw new AemetException(HttpStatus.BAD_GATEWAY,
                    "AEMET no devolvió datos: " + envelope.descripcion());
        }
        return fetch(URI.create(envelope.datos()), type);
    }
    
    private AemetException translate(RestClientResponseException e) {
        return switch (e.getStatusCode().value()) {
            case 404 -> new AemetException(HttpStatus.NOT_FOUND, "No hay datos en AEMET para ese municipio", e);
            case 429 -> new AemetException(HttpStatus.TOO_MANY_REQUESTS,
                    "AEMET ha limitado las peticiones. Inténtalo de nuevo en unos segundos", e);
            case 401, 403 -> new AemetException(HttpStatus.BAD_GATEWAY, "AEMET rechazó la API key", e);
            default -> new AemetException(HttpStatus.BAD_GATEWAY, "Error de AEMET: " + e.getStatusCode(), e);
        };
    }
}
