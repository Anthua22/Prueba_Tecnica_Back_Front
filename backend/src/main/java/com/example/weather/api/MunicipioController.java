package com.example.weather.api;

import com.example.weather.api.dto.MunicipioResponse;
import com.example.weather.service.MunicipioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/municipios")
@Validated
@Tag(name = "Municipios")
public class MunicipioController {

    private final MunicipioService municipioService;

    public MunicipioController(MunicipioService municipioService) {
        this.municipioService = municipioService;
    }

    @GetMapping
    @Operation(summary = "Busca municipios cuyo nombre empieza por el prefijo indicado",
            description = "Búsqueda sin distinguir mayúsculas ni acentos.")
    public List<MunicipioResponse> search(
            @Parameter(description = "Prefijo del nombre del municipio", example = "Ag")
            @RequestParam @NotBlank String nombre) {
        return municipioService.searchByPrefix(nombre).stream()
                .map(ApiMapper::toResponse)
                .toList();
    }
}
