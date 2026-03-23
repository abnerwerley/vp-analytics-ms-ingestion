package com.ingestion.vp_analytics.adapters.output.persistence.repository;

import com.ingestion.vp_analytics.adapters.output.persistence.entity.SpreadsheetUploadEntity;
import com.ingestion.vp_analytics.adapters.output.persistence.entity.TransactionEntity;
import com.ingestion.vp_analytics.domain.exception.EntityNotFoundException;
import com.ingestion.vp_analytics.domain.model.SpreadsheetUpload;
import com.ingestion.vp_analytics.domain.model.Transaction;
import com.ingestion.vp_analytics.domain.model.UploadStatus;
import com.ingestion.vp_analytics.domain.ports.output.TransactionRepositoryPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@AllArgsConstructor
public class JpaTransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final SpreadsheetUploadJpaRepository uploadRepository;
    private final TransactionJpaRepository transactionRepository;

    @Override
    public boolean existsByFileHash(final String fileHash) {
        return uploadRepository.existsByFileHash(fileHash);
    }

    @Override
    public SpreadsheetUpload saveUpload(final SpreadsheetUpload upload) {
        uploadRepository.save(
                new SpreadsheetUploadEntity(
                        upload.id(), upload.fileHash(), upload.originalFileName(),
                        upload.status(), upload.createdAt(), upload.updatedAt()));
        return upload;
    }

    @Override
    public SpreadsheetUpload updateUploadStatus(final String uploadId, final UploadStatus status) {
        final SpreadsheetUploadEntity entity = uploadRepository.findById(uploadId).orElseThrow(
                () -> new EntityNotFoundException("Upload not found " + uploadId));
        entity.setStatus(status);
        entity.setUpdatedAt(LocalDateTime.now());
        uploadRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public void saveTransactions(final List<Transaction> transactions, final String uploadId) {
        final List<TransactionEntity> entities = transactions.stream()
                .map(t -> new TransactionEntity(uploadId, t.date(), t.transactionType(),
                        t.expenseCategory(), t.revenueCategory(), t.description(),
                        t.isNewCustomer(), t.firstPurchaseDate(), t.value()))
                .toList();
        transactionRepository.saveAll(entities);
    }

    private SpreadsheetUpload toDomain(final SpreadsheetUploadEntity entity) {
        return new SpreadsheetUpload(entity.getId(), entity.getFileHash(), entity.getOriginalFileName(),
                entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
