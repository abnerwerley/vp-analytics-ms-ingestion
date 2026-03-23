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

    public ProcessSpreadsheetUseCase(TransactionRepositoryPort repository,
                                     SpreadsheetExtractorPort extractor, TransactionEventPublisherPort publisher) {
        this.repository = repository;
        this.extractor = extractor;
        this.publisher = publisher;
    }

    @Override
    public void execute(MultipartFile file, String clientId) {
        byte[] fileBytes = readBytes(file);
        String fileHash = computeSha256(fileBytes);

        if (repository.existsByFileHash(fileHash)) {
            throw new DuplicateFileException("File is duplicated: " + fileHash);
        }
        SpreadsheetUpload newUpload = new SpreadsheetUpload(
                UUID.randomUUID().toString(), fileHash, file.getOriginalFilename(),
                UploadStatus.RECEIVED, LocalDateTime.now(), LocalDateTime.now());

        SpreadsheetUpload upload = repository.saveUpload(newUpload);

        try {
            repository.updateUploadStatus(upload.id(), UploadStatus.PROCESSING);

            List<Transaction> transactions = extractor.extract(new ByteArrayInputStream(fileBytes), file.getName(), clientId);
            repository.saveTransactions(transactions, upload.id());
            repository.updateUploadStatus(upload.id(), UploadStatus.SUCCESS);
            publisher.publish(clientId, upload.id(), transactions);
        } catch (Exception e) {
            repository.updateUploadStatus(upload.id(), UploadStatus.FAILED);
            throw new ProcessSpreadSheetException("Failed to process spreadsheet: " + e.getMessage());
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            log.error("Failed to read bytes from file={}", file.getOriginalFilename(), e);
            throw new ProcessSpreadSheetException("Could not read file bytes.");
        }
    }

    private String computeSha256(byte[] fileBytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(fileBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new ProcessSpreadSheetException("SHA-256 not available.");
        }
    }
}
