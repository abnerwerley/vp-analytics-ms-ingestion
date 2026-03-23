package com.ingestion.vp_analytics.adapters.output.messaging;

import com.ingestion.vp_analytics.domain.model.Transaction;
import com.ingestion.vp_analytics.domain.ports.output.TransactionEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KafkaTransactionEventPublisherAdapter implements TransactionEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaTransactionEventPublisherAdapter.class);

    private final KafkaTemplate<String, TransactionsIngestedEvent> kafkaTemplate;
    private final String topic;

    public KafkaTransactionEventPublisherAdapter(KafkaTemplate<String, TransactionsIngestedEvent> kafkaTemplate,
                                                 @Value("${kafka.topics.transactions-ingested}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(String clientId, String uploadId, List<Transaction> transactions) {
        TransactionsIngestedEvent event = new TransactionsIngestedEvent(clientId, uploadId, transactions.size(), transactions);

        kafkaTemplate.send(topic, uploadId, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to public event for uploadId={}", uploadId, ex);
                    } else {
                        log.info("Event published for uploadId ={}, count = {}", uploadId, transactions.size());
                    }
                });
    }
}
