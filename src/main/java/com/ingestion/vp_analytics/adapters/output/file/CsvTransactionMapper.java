package com.ingestion.vp_analytics.adapters.output.file;

import com.ingestion.vp_analytics.domain.model.EExpenseCategories;
import com.ingestion.vp_analytics.domain.model.ERevenueCategories;
import com.ingestion.vp_analytics.domain.model.ETransactionType;
import com.ingestion.vp_analytics.domain.model.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

//Column mapping:
//0 = date (dd/MM/yyyy)
//1 = transactionType
//2 = expenseCategory
//3 = revenueCategory
//4 = description
//5 = customerId
//6 = isNewCustomer
//7 = firstPurchaseDate (may be empty, dd/MM/yyyy)
//8 = value (Brazilian decimal format)

@Component
public class CsvTransactionMapper {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Transaction map(String[] row, String clientId) {
        LocalDate firstPurchase = null;
        if (!row[7].trim().isEmpty()) {
            firstPurchase = LocalDate.parse(row[7].trim(), DATE_FORMAT);
        }
        return new Transaction(
                clientId,
                LocalDate.parse(row[0].trim(), DATE_FORMAT),
                ETransactionType.labelOf(row[1].trim()),
                EExpenseCategories.labelOf(row[2].trim()),
                ERevenueCategories.labelOf(row[3].trim()),
                row[4].trim(),
                row[5].trim(),
                Boolean.parseBoolean(row[6].trim()),
                firstPurchase,
                new BigDecimal(row[8].trim().replace(",", "")));
    }
}
