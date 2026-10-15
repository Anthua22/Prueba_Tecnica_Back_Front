package com.example.weather.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.weather.domain.Forecast;
import com.example.weather.domain.PrecipitationProbability;
import com.example.weather.domain.TemperatureUnit;
import com.example.weather.service.ForecastService;
import com.example.weather.service.MunicipioService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ForecastController.class, MunicipioController.class})
class ForecastControllerTest {

    @Autowired
    MockMvc mvc;
    @MockBean
    ForecastService forecastService;
    @MockBean
    MunicipioService municipioService;

    @Test
    void returnsForecastInRequestedUnit() throws Exception {
        when(forecastService.getTomorrowForecast(eq("03002"), eq(TemperatureUnit.G_FAH)))
                .thenReturn(new Forecast(66.7, TemperatureUnit.G_FAH,
                        List.of(new PrecipitationProbability(5, "00-06"))));

        mvc.perform(get("/api/municipios/03002/prediccion").param("unidadTemperatura", "G_FAH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaTemperatura").value(66.7))
                .andExpect(jsonPath("$.unidadTemperatura").value("G_FAH"))
                .andExpect(jsonPath("$.probPrecipitacion[0].periodo").value("00-06"));
    }

    @Test
    void usesCelsiusWhenUnitIsOmitted() throws Exception {
        when(forecastService.getTomorrowForecast(eq("03002"), any()))
                .thenReturn(new Forecast(19.5, TemperatureUnit.G_CEL, List.of()));

        mvc.perform(get("/api/municipios/03002/prediccion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadTemperatura").value("G_CEL"));
    }

    @Test
    void rejectsUnknownUnit() throws Exception {
        mvc.perform(get("/api/municipios/03002/prediccion").param("unidadTemperatura", "KELVIN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMalformedMunicipioCode() throws Exception {
        mvc.perform(get("/api/municipios/abc/prediccion")).andExpect(status().isBadRequest());
    }
}
