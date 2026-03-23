package com.ingestion.vp_analytics.domain.ports.output;

import com.ingestion.vp_analytics.domain.model.SpreadsheetUpload;
import com.ingestion.vp_analytics.domain.model.Transaction;
import com.ingestion.vp_analytics.domain.model.UploadStatus;

import java.util.List;

public interface TransactionRepositoryPort {
    boolean existsByFileHash(final String fileHash);

    SpreadsheetUpload saveUpload(final SpreadsheetUpload upload);

    SpreadsheetUpload updateUploadStatus(final String uploadId, final UploadStatus status);

    void saveTransactions(final List<Transaction> transactions, final String fileId);
}
