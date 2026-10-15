package com.example.weather.api;

import com.example.weather.api.dto.ForecastResponse;
import com.example.weather.domain.TemperatureUnit;
import com.example.weather.service.ForecastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/municipios/{codigo}/prediccion")
@Validated
@Tag(name = "Predicción")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GetMapping
    @Operation(summary = "Previsión del día siguiente para un municipio")
    public ForecastResponse tomorrow(
            @Parameter(description = "Código de municipio (5 dígitos)", example = "03002")
            @PathVariable @Pattern(regexp = "\\d{5}", message = "El código debe tener 5 dígitos") String codigo,
            @Parameter(description = "Unidad de temperatura. Por defecto G_CEL.")
            @RequestParam(defaultValue = "G_CEL") TemperatureUnit unidadTemperatura) {
        return ApiMapper.toResponse(forecastService.getTomorrowForecast(codigo, unidadTemperatura));
    }
}
