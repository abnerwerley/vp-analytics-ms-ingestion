package com.ingestion.vp_analytics.domain.model;

import lombok.Getter;

@Getter
public enum ETransactionType {
    REVENUE("Receita"),
    EXPENSE("Despesa"),
    NOT_CATEGORIZED("Não Categorizado");

    private final String label;

    ETransactionType(final String label) {
        this.label = label;
    }

    public static ETransactionType labelOf(final String label) {
        for (final ETransactionType value : values()) {
            if (value.label.equals(label)) {
                return value;
            }
        }
        return NOT_CATEGORIZED;
    }
}