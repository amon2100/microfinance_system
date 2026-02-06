package com.sacco.model;

import java.math.BigDecimal;

public class Loan {
    private final long id;
    private final long memberId;
    private final String memberName;
    private final BigDecimal principal;
    private final BigDecimal interestRate;
    private final int termMonths;
    private final String issuedAt;
    private final String status;
    private final BigDecimal outstandingBalance;
    private final Long createdBy;
    private final Long approvedBy;
    private final Long disbursedBy;

    public Loan(long id, long memberId, String memberName, BigDecimal principal, BigDecimal interestRate,
                int termMonths, String issuedAt, String status, BigDecimal outstandingBalance,
                Long createdBy, Long approvedBy, Long disbursedBy) {
        this.id = id;
        this.memberId = memberId;
        this.memberName = memberName;
        this.principal = principal;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.issuedAt = issuedAt;
        this.status = status;
        this.outstandingBalance = outstandingBalance;
        this.createdBy = createdBy;
        this.approvedBy = approvedBy;
        this.disbursedBy = disbursedBy;
    }

    public long getId() {
        return id;
    }

    public long getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public String getIssuedAt() {
        return issuedAt;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public Long getDisbursedBy() {
        return disbursedBy;
    }
}
