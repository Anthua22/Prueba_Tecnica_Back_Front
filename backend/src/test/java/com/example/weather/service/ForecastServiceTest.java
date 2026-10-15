package com.example.weather.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.weather.client.AemetClient;
import com.example.weather.client.dto.AemetPrediccionDiaria.Dia;
import com.example.weather.client.dto.AemetPrediccionDiaria.ProbPrecipitacion;
import com.example.weather.client.dto.AemetPrediccionDiaria.Temperatura;
import com.example.weather.domain.Forecast;
import com.example.weather.domain.PrecipitationProbability;
import com.example.weather.domain.TemperatureUnit;
import com.example.weather.exception.AemetException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

class ForecastServiceTest {

    // Hoy: 16/11/2021 → mañana: 17/11/2021
    private final Clock clock = Clock.fixed(Instant.parse("2021-11-16T10:00:00Z"), ZoneId.of("Europe/Madrid"));
    private final AemetClient client = mock(AemetClient.class);
    private final ForecastService service = new ForecastService(client, clock);

    private List<Dia> days() {
        Dia today = new Dia("2021-11-16T00:00:00", List.of(), new Temperatura(30, 30));
        Dia tomorrow = new Dia("2021-11-17T00:00:00",
                List.of(new ProbPrecipitacion(5, "00-06"), new ProbPrecipitacion(10, "06-12"),
                        new ProbPrecipitacion(10, "12-18"), new ProbPrecipitacion(5, "18-24"),
                        new ProbPrecipitacion(10, "00-24")),
                new Temperatura(24, 15)); // media 19,5 ºC
        return List.of(today, tomorrow);
    }

    @Test
    void usesTomorrowAndDefaultsToCelsiusValues() {
        when(client.getDailyForecast("03002")).thenReturn(days());

        Forecast f = service.getTomorrowForecast("03002", TemperatureUnit.G_CEL);

        assertThat(f.mediaTemperatura()).isEqualTo(19.5);
        assertThat(f.unidadTemperatura()).isEqualTo(TemperatureUnit.G_CEL);
        assertThat(f.probPrecipitacion()).containsExactly(
                new PrecipitationProbability(5, "00-06"),
                new PrecipitationProbability(10, "06-12"),
                new PrecipitationProbability(10, "12-18"),
                new PrecipitationProbability(5, "18-24"));
    }

    @Test
    void convertsToFahrenheit() {
        when(client.getDailyForecast("03002")).thenReturn(days());

        Forecast f = service.getTomorrowForecast("03002", TemperatureUnit.G_FAH);

        assertThat(f.mediaTemperatura()).isEqualTo(67.1); // 19,5 ºC
        assertThat(f.unidadTemperatura()).isEqualTo(TemperatureUnit.G_FAH);
    }

    @Test
    void failsWhenTomorrowIsMissing() {
        when(client.getDailyForecast("03002")).thenReturn(List.of(
                new Dia("2021-11-16T00:00:00", List.of(), new Temperatura(20, 10))));

        assertThatThrownBy(() -> service.getTomorrowForecast("03002", TemperatureUnit.G_CEL))
                .isInstanceOf(AemetException.class);
    }
}
