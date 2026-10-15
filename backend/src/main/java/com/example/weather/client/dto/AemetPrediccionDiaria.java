package com.example.weather.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Subconjunto de la predicción diaria por municipio de AEMET. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AemetPrediccionDiaria(Prediccion prediccion) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Prediccion(List<Dia> dia) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Dia(String fecha, List<ProbPrecipitacion> probPrecipitacion, Temperatura temperatura) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProbPrecipitacion(Integer value, String periodo) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Temperatura(Integer maxima, Integer minima) {
    }
}
