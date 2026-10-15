package com.example.weather.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Municipio tal y como lo devuelve AEMET (solo los campos que usamos). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AemetMunicipio(String id, String nombre) {
}
