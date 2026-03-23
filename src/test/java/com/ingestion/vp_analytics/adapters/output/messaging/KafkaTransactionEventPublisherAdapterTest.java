package com.ingestion.vp_analytics.adapters.output.messaging;

import com.ingestion.vp_analytics.adapters.output.persistence.repository.JpaClientRepositoryAdapter;
import com.ingestion.vp_analytics.adapters.output.persistence.repository.JpaTransactionRepositoryAdapter;
import com.ingestion.vp_analytics.domain.model.ERevenueCategories;
import com.ingestion.vp_analytics.domain.model.ETransactionType;
import com.ingestion.vp_analytics.domain.model.Transaction;
import com.ingestion.vp_analytics.domain.ports.input.GenerateClientInputPort;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "vpa.transactions.ingested")
@DirtiesContext
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class KafkaTransactionEventPublisherAdapterTest {

    @MockitoBean
    private JpaTransactionRepositoryAdapter repository;

    @MockitoBean
    private GenerateClientInputPort useCase;

    @MockitoBean
    private JpaClientRepositoryAdapter clientRepository;

    @Autowired
    private KafkaTransactionEventPublisherAdapter publisher;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private KafkaConsumer<String, String> consumer;

    public static final String TOPIC = "vpa.transactions.ingested";

    private static final String CLIENT_ID = "client-id-12321";

    public static final List<Transaction> TRANSACTIONS = List.of(
            new Transaction(CLIENT_ID, LocalDate.of(2025, 1, 15), ETransactionType.REVENUE,
                    null, ERevenueCategories.REFERRAL, "Google Ads", "CLI-001",
                    true, LocalDate.of(2025, 1, 15), new BigDecimal("4500.00"))
    );

    @BeforeEach
    void setupConsumer() {
        Map<String, Object> props = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafka);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC));
    }

    @AfterEach
    void afterAll() {
        consumer.close();
    }

    @Test
    void shoudPublishEventWithCorrectKey() {
        publisher.publish(CLIENT_ID, "upload-test-1", TRANSACTIONS);
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(3));

        assertEquals(1, records.count());
        ConsumerRecord<String, String> cRecord = records.iterator().next();
        assertEquals("upload-test-1", cRecord.key());
        assertTrue(cRecord.value().contains("upload-test-1"));
        assertTrue(cRecord.value().contains("transactionCount"));
    }

    @Test
    void shouldLogErrorWhenKafkaFails() {
        KafkaTemplate<String, TransactionsIngestedEvent> failingTemplate = mock(KafkaTemplate.class);
        CompletableFuture<SendResult<String, TransactionsIngestedEvent>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka is down"));

        when(failingTemplate.send(anyString(), anyString(), any())).thenReturn(failedFuture);

        KafkaTransactionEventPublisherAdapter failingPublisher = new KafkaTransactionEventPublisherAdapter(failingTemplate, TOPIC);
        assertDoesNotThrow(() -> failingPublisher.publish(CLIENT_ID, "upload-fail", TRANSACTIONS));
    }
}
