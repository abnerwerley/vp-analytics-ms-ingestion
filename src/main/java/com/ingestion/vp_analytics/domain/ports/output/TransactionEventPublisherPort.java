package com.ingestion.vp_analytics.domain.ports.output;

import com.ingestion.vp_analytics.domain.model.Transaction;

import java.util.List;

public interface TransactionEventPublisherPort {
    void publish(String clientId, String uploadId, List<Transaction> transactions);
}
