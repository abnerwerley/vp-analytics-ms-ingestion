package com.ingestion.vp_analytics.domain.ports.output;

import com.ingestion.vp_analytics.domain.model.Client;

import java.util.Optional;

public interface ClientRepositoryPort {
    Client save(Client client);

    boolean existsByEmail(String email);

    Optional<Client> findClientById(String clientId);
}
