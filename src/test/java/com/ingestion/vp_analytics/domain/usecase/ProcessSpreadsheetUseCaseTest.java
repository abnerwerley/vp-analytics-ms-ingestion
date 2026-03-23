package com.ingestion.vp_analytics.domain.usecase;

import com.ingestion.vp_analytics.domain.exception.DuplicateFileException;
import com.ingestion.vp_analytics.domain.model.ERevenueCategories;
import com.ingestion.vp_analytics.domain.model.ETransactionType;
import com.ingestion.vp_analytics.domain.model.SpreadsheetUpload;
import com.ingestion.vp_analytics.domain.model.Transaction;
import com.ingestion.vp_analytics.domain.model.UploadStatus;
import com.ingestion.vp_analytics.domain.ports.output.SpreadsheetExtractorPort;
import com.ingestion.vp_analytics.domain.ports.output.TransactionEventPublisherPort;
import com.ingestion.vp_analytics.domain.ports.output.TransactionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessSpreadsheetUseCaseTest {

    @Mock
    private SpreadsheetExtractorPort extractor;

    @Mock
    private TransactionRepositoryPort repository;

    @Mock
    private TransactionEventPublisherPort publisher;

    private ProcessSpreadsheetUseCase useCase;

    private static final String CLIENT_ID = "client-id-12321";
    public static final String UPLOAD_ID = "id-123";
    public static final List<Transaction> TRANSACTIONS = List.of(
            new Transaction(CLIENT_ID, LocalDate.of(2025, 1, 15), ETransactionType.REVENUE,
                    null, ERevenueCategories.REFERRAL, "Google Ads", "CLI-001",
                    true, LocalDate.of(2025, 1, 15), new BigDecimal("4500.00"))
    );

    @BeforeEach
    void setup() {
        useCase = new ProcessSpreadsheetUseCase(repository, extractor, publisher);
    }

    @Test
    void shouldProcessFileCompletely() {
        MockMultipartFile file = new MockMultipartFile("file", "planilha.csv", "text/csv",
                "content".getBytes());
        SpreadsheetUpload fileSaved = new SpreadsheetUpload(UPLOAD_ID, "any-hash", "planilha.csv",
                UploadStatus.RECEIVED, LocalDateTime.now(), LocalDateTime.now());

        when(repository.existsByFileHash(anyString())).thenReturn(false);
        when(repository.saveUpload(any())).thenReturn(fileSaved);
        when(extractor.extract(any(), anyString(), anyString())).thenReturn(TRANSACTIONS);

        useCase.execute(file, CLIENT_ID);

        ArgumentCaptor<UploadStatus> statusCaptor = ArgumentCaptor.forClass(UploadStatus.class);
        verify(repository, times(2))
                .updateUploadStatus(eq(UPLOAD_ID), statusCaptor.capture());
        assertThat(statusCaptor.getAllValues()).containsExactly(UploadStatus.PROCESSING, UploadStatus.SUCCESS);

        verify(repository).saveTransactions(TRANSACTIONS, UPLOAD_ID);
        verify(publisher).publish(CLIENT_ID, UPLOAD_ID, TRANSACTIONS);
    }

    @Test
    void shouldThrowDuplicateFileException() {
        MockMultipartFile file = new MockMultipartFile("file", "planilha.csv", "text/csv",
                "content".getBytes());

        when(repository.existsByFileHash(anyString())).thenReturn(true);

        assertThrows(DuplicateFileException.class, () -> useCase.execute(file, CLIENT_ID));

        verify(repository, never()).saveUpload(any());
        verify(extractor, never()).extract(any(), anyString(), anyString());
        verify(publisher, never()).publish(anyString(), anyString(), any());
    }

    @Test
    void shouldMarkStatusAsFailedWhenExtractionFails() {
        MockMultipartFile file = new MockMultipartFile("file", "broken.csv", "text/csv", "bad".getBytes());
        SpreadsheetUpload fileSaved = new SpreadsheetUpload("id-456", "any-hash", "broken.csv",
                UploadStatus.RECEIVED, LocalDateTime.now(), LocalDateTime.now());

        when(repository.existsByFileHash(anyString())).thenReturn(false);
        when(repository.saveUpload(any())).thenReturn(fileSaved);
        when(repository.updateUploadStatus(anyString(), any(UploadStatus.class))).thenReturn(fileSaved);
        when(extractor.extract(any(), anyString(), anyString())).thenThrow(new RuntimeException("Malformed CSV")); //

        RuntimeException exception = assertThrows(RuntimeException.class, () -> useCase.execute(file, CLIENT_ID));

        assertEquals("Failed to process spreadsheet: Malformed CSV", exception.getMessage());
        verify(repository).updateUploadStatus(fileSaved.id(), UploadStatus.FAILED);
        verify(publisher, never()).publish(anyString(), anyString(), any());
    }


    @Test
    void shouldMarkStatusAsFailedWhenPersistenceFails() {
        MockMultipartFile file = new MockMultipartFile("file", "jan.csv", "text/csv", "content".getBytes());
        SpreadsheetUpload fileSaved = new SpreadsheetUpload("id-789", "any-hash", "jan.csv",
                UploadStatus.RECEIVED, LocalDateTime.now(), LocalDateTime.now());

        when(repository.existsByFileHash(anyString())).thenReturn(false);
        when(repository.saveUpload(any())).thenReturn(fileSaved);
        when(repository.updateUploadStatus(anyString(), any(UploadStatus.class))).thenReturn(fileSaved);
        when(extractor.extract(any(), anyString(), anyString())).thenReturn(TRANSACTIONS);
        doThrow(new RuntimeException("Db error")).when(repository).saveTransactions(TRANSACTIONS, UPLOAD_ID); //

        assertThrows(RuntimeException.class, () -> useCase.execute(file, CLIENT_ID));

        verify(repository).updateUploadStatus(fileSaved.id(), UploadStatus.FAILED);
        verify(publisher, never()).publish(anyString(), anyString(), any());
    }


    @Test
    void shouldThrowExceptionWhenReadBytesFails() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                useCase.execute(file, CLIENT_ID));

        assertEquals("Could not read file bytes.", exception.getMessage());
        verify(repository, never()).saveUpload(any());
        verify(extractor, never()).extract(any(), anyString(), anyString());
        verify(publisher, never()).publish(anyString(), anyString(), any());
    }

    @Test
    void shouldThrowWhenSha256IsNotAvailable() {
        MockMultipartFile file = new MockMultipartFile("file", "planilha.csv",
                "text/csv", "content".getBytes());

        try (MockedStatic<MessageDigest> mockedDigest = mockStatic(MessageDigest.class)) {
            mockedDigest.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("Forced for test"));

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    useCase.execute(file, CLIENT_ID));

            assertEquals("SHA-256 not available.", exception.getMessage());
        }
    }
}
