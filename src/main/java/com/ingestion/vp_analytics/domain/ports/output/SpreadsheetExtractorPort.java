package com.ingestion.vp_analytics.domain.ports.output;

import com.ingestion.vp_analytics.domain.model.Transaction;

import java.io.InputStream;
import java.util.List;

public interface SpreadsheetExtractorPort {
    List<Transaction> extract(final InputStream inputStream, final String fileName, final String clientId);
}
