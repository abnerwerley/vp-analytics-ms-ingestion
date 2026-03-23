package com.ingestion.vp_analytics.domain.ports.output;

import com.ingestion.vp_analytics.domain.model.Client;

import java.util.Optional;

public interface ClientRepositoryPort {
    Client save(final Client client);

    boolean existsByEmail(final String email);

    Optional<Client> findClientById(final String clientId);
}
