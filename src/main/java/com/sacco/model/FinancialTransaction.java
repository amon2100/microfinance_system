package com.sacco.model;

import java.math.BigDecimal;
import java.time.Instant;

public class FinancialTransaction {
    private final long id;
    private final long memberId;
    private final String type;
    private final BigDecimal amount;
    private final Instant createdAt;

    public FinancialTransaction(long id, long memberId, String type, BigDecimal amount, Instant createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.type = type;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getMemberId() {
        return memberId;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
