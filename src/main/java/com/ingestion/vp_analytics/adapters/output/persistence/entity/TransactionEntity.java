package com.ingestion.vp_analytics.adapters.output.persistence.entity;

import com.ingestion.vp_analytics.domain.model.EExpenseCategories;
import com.ingestion.vp_analytics.domain.model.ERevenueCategories;
import com.ingestion.vp_analytics.domain.model.ETransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@NoArgsConstructor
public class TransactionEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "upload_id", nullable = false)
    private String uploadId;

    @Column(name = "date", nullable = false, columnDefinition = "DATE")
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private ETransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "expense_category")
    private EExpenseCategories expenseCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "revenue_category")
    private ERevenueCategories revenueCategory;

    @Column(name = "description")
    private String description;

    @Column(name = "is_new_customer")
    private Boolean isNewCustomer;

    @Column(name = "first_purchase_date", columnDefinition = "DATE")
    private LocalDate firstPurchaseDate;

    @Column(name = "value", precision = 15, scale = 2)
    private BigDecimal value;

    public TransactionEntity(final String uploadId, final LocalDate date, final ETransactionType transactionType,
                             final EExpenseCategories expenseCategory, final ERevenueCategories revenueCategory,
                             final String description, final Boolean isNewCustomer, final LocalDate firstPurchaseDate,
                             final BigDecimal value) {
        this.uploadId = uploadId;
        this.date = date;
        this.transactionType = transactionType;
        this.expenseCategory = expenseCategory;
        this.revenueCategory = revenueCategory;
        this.description = description;
        this.isNewCustomer = isNewCustomer;
        this.firstPurchaseDate = firstPurchaseDate;
        this.value = value;
    }
}
