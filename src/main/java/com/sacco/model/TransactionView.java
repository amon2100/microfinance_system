package com.sacco.model;

import java.math.BigDecimal;

public class TransactionView {
    private final long id;
    private final String memberName;
    private final String type;
    private final BigDecimal amount;
    private final String createdAt;

    public TransactionView(long id, String memberName, String type, BigDecimal amount, String createdAt) {
        this.id = id;
        this.memberName = memberName;
        this.type = type;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
