package com.example.weather.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Municipio de España")
public record MunicipioResponse(
        @Schema(example = "03002") String codigo,
        @Schema(example = "Agost") String nombre) {
}
