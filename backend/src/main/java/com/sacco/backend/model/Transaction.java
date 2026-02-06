package com.sacco.backend.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    private Long reversedOf;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getReversedOf() {
        return reversedOf;
    }

    public void setReversedOf(Long reversedOf) {
        this.reversedOf = reversedOf;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
