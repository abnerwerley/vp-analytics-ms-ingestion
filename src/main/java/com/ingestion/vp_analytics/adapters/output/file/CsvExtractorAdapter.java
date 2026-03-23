package com.ingestion.vp_analytics.adapters.output.file;

import com.ingestion.vp_analytics.domain.exception.ProcessSpreadSheetException;
import com.ingestion.vp_analytics.domain.model.Transaction;
import com.ingestion.vp_analytics.domain.ports.output.SpreadsheetExtractorPort;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CsvExtractorAdapter implements SpreadsheetExtractorPort {

    private final CsvTransactionMapper mapper;

    public CsvExtractorAdapter(final CsvTransactionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Transaction> extract(final InputStream inputStream, final String fileName, final String clientId) {
        try {
            final char separator = resolveSeparator(fileName);

            final CSVReader reader = new CSVReaderBuilder(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                    .withSkipLines(1)
                    .build();

            return reader.readAll().stream()
                    .filter(row -> !isBlankRow(row))
                    .filter(row -> row.length >= 8)
                    .map(row -> mapper.map(row, clientId))
                    .toList();
        } catch (final Exception e) {
            throw new ProcessSpreadSheetException("Failed to parse CSV: " + e.getMessage());
        }
    }

    private char resolveSeparator(final String fileName) {
        final String lower = fileName.toLowerCase();
        if (lower.endsWith(".tsv")) return '\t';
        return ',';
    }

    private boolean isBlankRow(final String[] row) {
        for (final String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) return false;
        }
        return true;
    }
}
