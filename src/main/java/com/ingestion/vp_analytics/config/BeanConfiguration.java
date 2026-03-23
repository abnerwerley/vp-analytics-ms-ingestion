package com.ingestion.vp_analytics.config;

import com.ingestion.vp_analytics.domain.ports.input.GenerateClientInputPort;
import com.ingestion.vp_analytics.domain.ports.output.ClientRepositoryPort;
import com.ingestion.vp_analytics.domain.ports.output.SpreadsheetExtractorPort;
import com.ingestion.vp_analytics.domain.ports.output.TransactionEventPublisherPort;
import com.ingestion.vp_analytics.domain.ports.output.TransactionRepositoryPort;
import com.ingestion.vp_analytics.domain.usecase.GenerateClientUseCase;
import com.ingestion.vp_analytics.domain.usecase.ProcessSpreadsheetUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public ProcessSpreadsheetUseCase processSpreadsheetUseCase(
            SpreadsheetExtractorPort extractor,
            TransactionRepositoryPort repository,
            TransactionEventPublisherPort publisher) {
        return new ProcessSpreadsheetUseCase(repository, extractor, publisher);
    }

    @Bean
    public GenerateClientInputPort generateClientUseCase(ClientRepositoryPort repositoryPort) {
        return new GenerateClientUseCase(repositoryPort);
    }
}
