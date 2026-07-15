package com.utp.lenasysabores.service;

import com.utp.lenasysabores.model.ReniecPersona;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Service
public class ReniecService {

    private final RestClient restClient;

    @Value("${decolecta.api.url}")
    private String apiUrl;

    @Value("${decolecta.api.token}")
    private String apiToken;

    public ReniecService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public Optional<ReniecPersona> consultarPorDni(String dni) {
        if (!StringUtils.hasText(apiToken) || dni == null || !dni.matches("\\d{8}")) {
            return Optional.empty();
        }

        try {
            ReniecPersona persona = restClient.get()
                    .uri(apiUrl + "?numero={dni}", dni)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ReniecPersona.class);

            if (persona == null || !StringUtils.hasText(persona.getFullName())) {
                return Optional.empty();
            }

            return Optional.of(persona);
        } catch (RestClientException ex) {
            return Optional.empty();
        }
    }
}
