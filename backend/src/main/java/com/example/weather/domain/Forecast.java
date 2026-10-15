package com.example.weather.domain;

import java.util.List;

public record Forecast(
        double mediaTemperatura,
        TemperatureUnit unidadTemperatura,
        List<PrecipitationProbability> probPrecipitacion) {
}
