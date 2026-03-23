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

    public CsvExtractorAdapter(CsvTransactionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Transaction> extract(InputStream inputStream, String fileName, String clientId) {
        try {
            char separator = resolveSeparator(fileName);

            CSVReader reader = new CSVReaderBuilder(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                    .withSkipLines(1)
                    .build();

            return reader.readAll().stream()
                    .filter(row -> !isBlankRow(row))
                    .filter(row -> row.length >= 8)
                    .map(row -> mapper.map(row, clientId))
                    .toList();
        } catch (Exception e) {
            throw new ProcessSpreadSheetException("Failed to parse CSV: " + e.getMessage());
        }
    }

    private char resolveSeparator(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".tsv")) return '\t';
        if (lower.endsWith(".csv")) return ',';
        return ',';
    }

    private boolean isBlankRow(String[] row) {
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) return false;
        }
        return true;
    }
}
