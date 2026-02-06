package com.sacco.model;

import java.math.BigDecimal;

public class TransactionRecord {
    private final long id;
    private final long memberId;
    private final String type;
    private final BigDecimal amount;
    private final Long reversedOf;

    public TransactionRecord(long id, long memberId, String type, BigDecimal amount, Long reversedOf) {
        this.id = id;
        this.memberId = memberId;
        this.type = type;
        this.amount = amount;
        this.reversedOf = reversedOf;
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

    public Long getReversedOf() {
        return reversedOf;
    }
}
