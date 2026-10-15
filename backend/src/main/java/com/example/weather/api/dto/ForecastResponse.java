package com.example.weather.api.dto;

import com.example.weather.domain.TemperatureUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Previsión del día siguiente")
public record ForecastResponse(
        @Schema(example = "66.7") double mediaTemperatura,
        @Schema(example = "G_FAH") TemperatureUnit unidadTemperatura,
        List<PrecipitacionResponse> probPrecipitacion) {
}
