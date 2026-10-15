package com.example.weather.service;

import com.example.weather.client.AemetClient;
import com.example.weather.client.dto.AemetPrediccionDiaria.Dia;
import com.example.weather.client.dto.AemetPrediccionDiaria.ProbPrecipitacion;
import com.example.weather.client.dto.AemetPrediccionDiaria.Temperatura;
import com.example.weather.domain.Forecast;
import com.example.weather.domain.PrecipitationProbability;
import com.example.weather.domain.TemperatureUnit;
import com.example.weather.exception.AemetException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ForecastService {

    /** Franjas de 6 horas. AEMET también devuelve franjas agregadas (00-12, 12-24, 00-24) que descartamos. */
    private static final Set<String> SIX_HOUR_PERIODS = Set.of("00-06", "06-12", "12-18", "18-24");

    private final AemetClient aemetClient;
    private final Clock clock;

    public ForecastService(AemetClient aemetClient, Clock clock) {
        this.aemetClient = aemetClient;
        this.clock = clock;
    }

    public Forecast getTomorrowForecast(String codigo, TemperatureUnit unit) {
        String tomorrow = LocalDate.now(clock).plusDays(1).toString(); // 2021-11-17
        Dia day = aemetClient.getDailyForecast(codigo).stream()
                .filter(d -> d.fecha() != null && d.fecha().startsWith(tomorrow))
                .findFirst()
                .orElseThrow(() -> new AemetException(HttpStatus.NOT_FOUND,
                        "AEMET no tiene previsión para mañana en ese municipio"));

        return new Forecast(
                averageTemperature(day.temperatura(), unit),
                unit,
                precipitation(day.probPrecipitacion()));
    }

    /** Temperatura media = (máxima + mínima) / 2, convertida a la unidad pedida y redondeada a 1 decimal. */
    private static double averageTemperature(Temperatura t, TemperatureUnit unit) {
        if (t == null || t.maxima() == null || t.minima() == null) {
            throw new AemetException(HttpStatus.BAD_GATEWAY, "AEMET no devolvió la temperatura de mañana");
        }
        double celsius = (t.maxima() + t.minima()) / 2.0;
        return Math.round(unit.fromCelsius(celsius) * 10.0) / 10.0;
    }

    private static List<PrecipitationProbability> precipitation(List<ProbPrecipitacion> raw) {
        List<ProbPrecipitacion> valid = raw == null ? List.of()
                : raw.stream().filter(p -> p.value() != null && p.periodo() != null).toList();
        List<ProbPrecipitacion> sixHour = valid.stream()
                .filter(p -> SIX_HOUR_PERIODS.contains(p.periodo()))
                .toList();
        return (sixHour.isEmpty() ? valid : sixHour).stream()
                .map(p -> new PrecipitationProbability(p.value(), p.periodo()))
                .toList();
    }
}
