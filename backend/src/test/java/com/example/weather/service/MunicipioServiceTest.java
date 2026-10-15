package com.example.weather.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.weather.client.AemetClient;
import com.example.weather.client.dto.AemetMunicipio;
import com.example.weather.domain.Municipio;
import java.util.List;
import org.junit.jupiter.api.Test;

class MunicipioServiceTest {

    private final AemetClient client = mock(AemetClient.class);
    private final MunicipioService service = new MunicipioService(client);

    @Test
    void filtersByPrefixAndStripsIdPrefixFromCode() {
        when(client.getMunicipios()).thenReturn(List.of(
                new AemetMunicipio("id03003", "Agres"),
                new AemetMunicipio("id03002", "Agost"),
                new AemetMunicipio("id40001", "Abades")));

        List<Municipio> result = service.searchByPrefix("Ag");

        assertThat(result).containsExactly(
                new Municipio("03002", "Agost"),
                new Municipio("03003", "Agres"));
    }

    @Test
    void ignoresCaseAndAccents() {
        when(client.getMunicipios()).thenReturn(List.of(
                new AemetMunicipio("id28079", "Ágreda"),
                new AemetMunicipio("id41091", "Sevilla")));

        assertThat(service.searchByPrefix("agr")).extracting(Municipio::nombre).containsExactly("Ágreda");
    }

    @Test
    void returnsEmptyListWhenNothingMatches() {
        when(client.getMunicipios()).thenReturn(List.of(new AemetMunicipio("id40001", "Abades")));

        assertThat(service.searchByPrefix("zzz")).isEmpty();
    }
}
