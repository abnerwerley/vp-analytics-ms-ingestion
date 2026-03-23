package com.ingestion.vp_analytics.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transaction(
        String clientId,
        LocalDate date,
        ETransactionType transactionType,
        EExpenseCategories expenseCategory,
        ERevenueCategories revenueCategory,
        String description,
        String customerId,
        boolean isNewCustomer,
        LocalDate firstPurchaseDate,
        BigDecimal value
) {
}
