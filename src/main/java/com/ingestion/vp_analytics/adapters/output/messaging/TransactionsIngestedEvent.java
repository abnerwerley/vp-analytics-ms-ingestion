package com.ingestion.vp_analytics.adapters.output.messaging;

import com.ingestion.vp_analytics.domain.model.Transaction;

import java.util.List;

public record TransactionsIngestedEvent(
        String clientId,
        String uploadId,
        int transactionCount,
        List<Transaction> transactions) {
}
