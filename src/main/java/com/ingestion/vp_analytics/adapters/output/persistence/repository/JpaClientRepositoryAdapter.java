package com.ingestion.vp_analytics.adapters.output.persistence.repository;

import com.ingestion.vp_analytics.adapters.output.persistence.entity.ClientEntity;
import com.ingestion.vp_analytics.domain.model.Client;
import com.ingestion.vp_analytics.domain.ports.output.ClientRepositoryPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class JpaClientRepositoryAdapter implements ClientRepositoryPort {

    private final ClientJpaRepository repository;

    @Override
    public Client save(final Client client) {
        repository.save(new ClientEntity(client.clientId(),
                client.name(),
                client.email(),
                client.createdAt()));
        return client;
    }

    @Override
    public boolean existsByEmail(final String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Optional<Client> findClientById(final String clientId) {
        return repository.findByClientId(clientId).map(c ->
                new Client(
                        c.getClientId(), c.getName(), c.getEmail(), c.getCreatedAt()
                )
        );
    }
}
