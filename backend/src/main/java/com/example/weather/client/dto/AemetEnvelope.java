package com.example.weather.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Respuesta "de sobre" de AEMET: no contiene los datos, sino la URL desde la que descargarlos. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AemetEnvelope(String descripcion, Integer estado, String datos) {
}
