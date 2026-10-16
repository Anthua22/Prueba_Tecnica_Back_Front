package com.example.weather.api;

import com.example.weather.api.dto.ForecastResponse;
import com.example.weather.api.dto.MunicipioResponse;
import com.example.weather.api.dto.PrecipitacionResponse;
import com.example.weather.domain.Forecast;
import com.example.weather.domain.Municipio;

/** Traduce el modelo de dominio al contrato público de nuestra API. */
final class ApiMapper {

    private ApiMapper() {
    }

    static MunicipioResponse toResponse(Municipio m) {
        return new MunicipioResponse(m.codigo(), m.nombre());
    }

    static ForecastResponse toResponse(Forecast f) {
        return new ForecastResponse(
                f.mediaTemperatura(),
                f.unidadTemperatura(),
                f.probPrecipitacion().stream()
                        .map(p -> new PrecipitacionResponse(100 - p.probabilidad(), p.periodo()))
                        .toList());
    }
}
