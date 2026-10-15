package com.example.weather.service;

import com.example.weather.client.AemetClient;
import com.example.weather.client.dto.AemetMunicipio;
import com.example.weather.domain.Municipio;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class MunicipioService {

    private static final String AEMET_ID_PREFIX = "id";

    private final AemetClient aemetClient;

    public MunicipioService(AemetClient aemetClient) {
        this.aemetClient = aemetClient;
    }

    /** Municipios cuyo nombre empieza por {@code prefix}, ignorando mayúsculas y acentos. */
    public List<Municipio> searchByPrefix(String prefix) {
        String needle = normalize(prefix);
        return aemetClient.getMunicipios().stream()
                .filter(m -> m.id() != null && m.nombre() != null)
                .filter(m -> normalize(m.nombre()).startsWith(needle))
                .map(MunicipioService::toDomain)
                .sorted(Comparator.comparing(m -> normalize(m.nombre())))
                .toList();
    }

    private static Municipio toDomain(AemetMunicipio m) {
        String codigo = m.id().startsWith(AEMET_ID_PREFIX) ? m.id().substring(AEMET_ID_PREFIX.length()) : m.id();
        return new Municipio(codigo, m.nombre());
    }

    static String normalize(String text) {
        return Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
