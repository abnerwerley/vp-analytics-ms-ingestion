package com.ingestion.vp_analytics.adapters.output.persistence.repository;

import com.ingestion.vp_analytics.adapters.output.persistence.entity.SpreadsheetUploadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpreadsheetUploadJpaRepository extends JpaRepository<SpreadsheetUploadEntity, String> {
    boolean existsByFileHash(String fileHash);
}
