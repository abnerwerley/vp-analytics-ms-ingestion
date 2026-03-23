package com.ingestion.vp_analytics.adapters.output.persistence;

import com.ingestion.vp_analytics.adapters.output.persistence.repository.JpaTransactionRepositoryAdapter;
import com.ingestion.vp_analytics.domain.exception.EntityNotFoundException;
import com.ingestion.vp_analytics.domain.model.EExpenseCategories;
import com.ingestion.vp_analytics.domain.model.ERevenueCategories;
import com.ingestion.vp_analytics.domain.model.ETransactionType;
import com.ingestion.vp_analytics.domain.model.SpreadsheetUpload;
import com.ingestion.vp_analytics.domain.model.Transaction;
import com.ingestion.vp_analytics.domain.model.UploadStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaTransactionRepositoryAdapter.class)
class JpaTransactionRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private JpaTransactionRepositoryAdapter repositoryAdapter;

    public static final String ANY_HASH = "any-hash";

    private static final String CLIENT_ID = "client-id-12321";

    @Test
    void shouldReturnFalseIfHashNotFound() {
        assertFalse(repositoryAdapter.existsByFileHash(ANY_HASH));
    }

    @Test
    void shouldReturnTrueAfterSavingUpload() {
        repositoryAdapter.saveUpload(buildUpload());
        assertTrue(repositoryAdapter.existsByFileHash(buildUpload().fileHash()));
    }

    @Test
    void ShouldUpdateUploadStatus() {
        repositoryAdapter.saveUpload(buildUpload());
        SpreadsheetUpload upload = buildUpload();
        repositoryAdapter.saveUpload(upload);

        SpreadsheetUpload uploadUpdated = repositoryAdapter.updateUploadStatus(upload.id(), UploadStatus.PROCESSING);

        assertEquals(UploadStatus.PROCESSING, uploadUpdated.status());
    }

    @Test
    void shouldPersistTransactions() {
        SpreadsheetUpload upload = buildUpload();
        repositoryAdapter.saveUpload(upload);

        List<Transaction> transactions = List.of(
                new Transaction(CLIENT_ID, LocalDate.of(2026, 6, 15), ETransactionType.EXPENSE, EExpenseCategories.TAXES,
                        null, "ICMS", null, false, null, new BigDecimal("2897.98")),
                new Transaction(CLIENT_ID, LocalDate.of(2026, 5, 15), ETransactionType.REVENUE, null,
                        ERevenueCategories.INSTALLMENT, "CLI-15", "client15@gmail.com", false, LocalDate.now(), new BigDecimal("2897.98"))
        );
        repositoryAdapter.saveTransactions(transactions, upload.id());
        assertTrue(repositoryAdapter.existsByFileHash(upload.fileHash()));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUpdatingUploadStatus() {
        String nonExistentId = UUID.randomUUID().toString();

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> repositoryAdapter.updateUploadStatus(nonExistentId, UploadStatus.PROCESSING));

        assertEquals("Upload not found " + nonExistentId, exception.getMessage());
    }

    private SpreadsheetUpload buildUpload() {
        return new SpreadsheetUpload(UUID.randomUUID().toString(),
                JpaTransactionRepositoryAdapterTest.ANY_HASH, "filename.csv", UploadStatus.RECEIVED,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
