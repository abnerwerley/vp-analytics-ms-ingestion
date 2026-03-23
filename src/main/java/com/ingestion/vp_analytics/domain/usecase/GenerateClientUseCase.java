package com.ingestion.vp_analytics.domain.usecase;

import com.ingestion.vp_analytics.domain.model.Client;
import com.ingestion.vp_analytics.domain.ports.input.GenerateClientInputPort;
import com.ingestion.vp_analytics.domain.ports.output.ClientRepositoryPort;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class GenerateClientUseCase implements GenerateClientInputPort {
    private final ClientRepositoryPort repository;

    public GenerateClientUseCase(ClientRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Map<String, String> generate(String name, String email) {
        Client client = repository.save(new Client(UUID.randomUUID().toString(), name, email, LocalDateTime.now()));
        return Map.of("clientId", client.clientId());
    }
}
