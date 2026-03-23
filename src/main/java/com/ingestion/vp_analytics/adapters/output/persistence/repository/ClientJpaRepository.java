package com.ingestion.vp_analytics.adapters.output.persistence.repository;

import com.ingestion.vp_analytics.adapters.output.persistence.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientJpaRepository extends JpaRepository<ClientEntity, String> {

    boolean existsByEmail(final String email);

    Optional<ClientEntity> findByClientId(final String clientId);
}
