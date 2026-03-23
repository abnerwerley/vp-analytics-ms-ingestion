package com.ingestion.vp_analytics.domain.usecase;

import com.ingestion.vp_analytics.domain.exception.DuplicateFileException;
import com.ingestion.vp_analytics.domain.exception.ProcessSpreadSheetException;
import com.ingestion.vp_analytics.domain.model.SpreadsheetUpload;
import com.ingestion.vp_analytics.domain.model.Transaction;
import com.ingestion.vp_analytics.domain.model.UploadStatus;
import com.ingestion.vp_analytics.domain.ports.input.ProcessSpreadsheetInputPort;
import com.ingestion.vp_analytics.domain.ports.output.SpreadsheetExtractorPort;
import com.ingestion.vp_analytics.domain.ports.output.TransactionEventPublisherPort;
import com.ingestion.vp_analytics.domain.ports.output.TransactionRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Slf4j
public class ProcessSpreadsheetUseCase implements ProcessSpreadsheetInputPort {

    private final TransactionRepositoryPort repository;
    private final SpreadsheetExtractorPort extractor;
    private final TransactionEventPublisherPort publisher;

    public ProcessSpreadsheetUseCase(final TransactionRepositoryPort repository,
                                     final SpreadsheetExtractorPort extractor,
                                     final TransactionEventPublisherPort publisher) {
        this.repository = repository;
        this.extractor = extractor;
        this.publisher = publisher;
    }

    @Override
    public void execute(final MultipartFile file, final String clientId) {
        final byte[] fileBytes = readBytes(file);
        final String fileHash = computeSha256(fileBytes);

        if (repository.existsByFileHash(fileHash)) {
            throw new DuplicateFileException("File is duplicated: " + fileHash);
        }
        final SpreadsheetUpload newUpload = new SpreadsheetUpload(
                UUID.randomUUID().toString(), fileHash, file.getOriginalFilename(),
                UploadStatus.RECEIVED, LocalDateTime.now(), LocalDateTime.now());

        final SpreadsheetUpload upload = repository.saveUpload(newUpload);

        try {
            repository.updateUploadStatus(upload.id(), UploadStatus.PROCESSING);

            final List<Transaction> transactions = extractor.extract(
                    new ByteArrayInputStream(fileBytes), file.getName(), clientId);
            repository.saveTransactions(transactions, upload.id());
            repository.updateUploadStatus(upload.id(), UploadStatus.SUCCESS);
            publisher.publish(clientId, upload.id(), transactions);
        } catch (final Exception e) {
            repository.updateUploadStatus(upload.id(), UploadStatus.FAILED);
            throw new ProcessSpreadSheetException("Failed to process spreadsheet: " + e.getMessage());
        }
    }

    private byte[] readBytes(final MultipartFile file) {
        try {
            return file.getBytes();
        } catch (final IOException e) {
            log.error("Failed to read bytes from file={}", file.getOriginalFilename(), e);
            throw new ProcessSpreadSheetException("Could not read file bytes.");
        }
    }

    private String computeSha256(final byte[] fileBytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(fileBytes));
        } catch (final NoSuchAlgorithmException e) {
            throw new ProcessSpreadSheetException("SHA-256 not available.");
        }
    }
}
