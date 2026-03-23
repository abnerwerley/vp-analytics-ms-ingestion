package com.ingestion.vp_analytics.adapters.output.file;

import com.ingestion.vp_analytics.domain.exception.ProcessSpreadSheetException;
import com.ingestion.vp_analytics.domain.model.EExpenseCategories;
import com.ingestion.vp_analytics.domain.model.ERevenueCategories;
import com.ingestion.vp_analytics.domain.model.ETransactionType;
import com.ingestion.vp_analytics.domain.model.Transaction;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

class CsvExtractorAdapterTest {

    private CsvExtractorAdapter csvExtractor;

    public static final String CSV_HEADER = "date;transactionType;expenseCategory;description;customerId;isNewCustomer;firstPurchaseDate;value";

    private static final String CLIENT_ID = "client-id-12321";

    @BeforeEach
    void setup() {
        csvExtractor = new CsvExtractorAdapter(new CsvTransactionMapper());
    }

    @Test
    void shouldParseAllValidRows() {
        String fileName = "transactions-sample.csv";
        List<Transaction> result = csvExtractor.extract(csv(fileName), fileName, CLIENT_ID);
        Transaction firstRow = result.getFirst();
        Transaction secondRow = result.get(1);
        Transaction thirdRow = result.get(2);

        assertEquals(3, result.size());

        assertEquals(LocalDate.of(2025, 1, 15), firstRow.date());
        assertEquals(ETransactionType.REVENUE, firstRow.transactionType());
        assertEquals(EExpenseCategories.NOT_CATEGORIZED, firstRow.expenseCategory());
        assertEquals(ERevenueCategories.REFERRAL, firstRow.revenueCategory());
        assertEquals("Google Ads campaign", firstRow.description());
        assertEquals("CLI-001", firstRow.customerId());
        assertTrue(firstRow.isNewCustomer());
        assertEquals(LocalDate.of(2025, 1, 15), firstRow.firstPurchaseDate());
        assertEquals(new BigDecimal("4500.00"), firstRow.value());

        assertEquals(LocalDate.of(2025, 1, 20), secondRow.date());
        assertEquals(ETransactionType.EXPENSE, secondRow.transactionType());
        assertEquals(EExpenseCategories.TAXES, secondRow.expenseCategory());
        assertEquals(ERevenueCategories.NOT_CATEGORIZED, secondRow.revenueCategory());
        assertEquals("Simples Nacional", secondRow.description());
        assertEquals("", secondRow.customerId());
        assertFalse(secondRow.isNewCustomer());
        assertNull(secondRow.firstPurchaseDate());
        assertEquals(new BigDecimal("1200.50"), secondRow.value());

        assertEquals(LocalDate.of(2025, 1, 30), thirdRow.date());
        assertEquals(ETransactionType.NOT_CATEGORIZED, thirdRow.transactionType());
        assertEquals(EExpenseCategories.SALES_TEAM, thirdRow.expenseCategory());
        assertEquals(ERevenueCategories.NOT_CATEGORIZED, thirdRow.revenueCategory());
        assertEquals("Operacional Vendas", thirdRow.description());
        assertEquals("", thirdRow.customerId());
        assertFalse(thirdRow.isNewCustomer());
        assertNull(thirdRow.firstPurchaseDate());
        assertEquals(new BigDecimal("6500.00"), thirdRow.value());
    }

    @Test
    void shouldHandleEmptyFirstPurchaseDate() {
        String fileName = "transactions-sample.csv";
        Transaction secondRow = csvExtractor.extract(csv(fileName), fileName, CLIENT_ID).get(1);

        assertEquals(ETransactionType.EXPENSE, secondRow.transactionType());
        assertFalse(secondRow.isNewCustomer());
        assertNull(secondRow.firstPurchaseDate());
        assertEquals(new BigDecimal("1200.50"), secondRow.value());
    }

    @Test
    void shouldReturnEmptyListForMalformedRows() {
        byte[] malformed = (CSV_HEADER + "not;enough;columns also;bad").getBytes();
        List<Transaction> result = csvExtractor.extract(new ByteArrayInputStream(malformed), "", CLIENT_ID);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnShorterListRow7Columns() {
        String fileName = "transactions-sample-short-row.csv";
        List<Transaction> result = csvExtractor.extract(csv(fileName), fileName, CLIENT_ID);
        assertEquals(1, result.size());
    }

    @Test
    void shouldThrowException() throws IOException, CsvException {
        CSVReader csvReader = Mockito.mock(CSVReader.class);
        when(csvReader.readAll()).thenThrow(ProcessSpreadSheetException.class);

        try {
            String fileName = "transactions-sample-comma-separator.csv";
            csvExtractor.extract(csv(fileName), fileName, CLIENT_ID);
            fail();
        } catch (ProcessSpreadSheetException e) {
            assertEquals("Failed to parse CSV: Text '15/0415/2025' could not be parsed at index 5", e.getMessage());
        }
    }

    @Test
    void shouldParseTsvFile() {
        String tsvFile = "tsv-file-sample.tsv";
        List<Transaction> resultTsvFile = csvExtractor.extract(csv(tsvFile), tsvFile, CLIENT_ID);
        assertEquals(3, resultTsvFile.size());
    }

    @Test
    void shouldReturnFalseWhenRowHasNonNullCell() throws Exception {
        Method isBlankRow = CsvExtractorAdapter.class
                .getDeclaredMethod("isBlankRow", String[].class);
        isBlankRow.setAccessible(true);

        String[] rowWithValue = {null, "value", null};
        boolean result = (boolean) isBlankRow.invoke(csvExtractor, (Object) rowWithValue);

        assertFalse(result);
    }

    private InputStream csv(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }
}

