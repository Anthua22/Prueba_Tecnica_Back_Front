package com.example.weather.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Probabilidad de precipitación en un periodo del día")
public record PrecipitacionResponse(
        @Schema(example = "10") int probabilidad,
        @Schema(example = "06-12") String periodo) {
}
