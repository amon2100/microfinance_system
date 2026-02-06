package com.sacco.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class LoanSyncRequest {
    @NotBlank
    private String externalId;
    @NotBlank
    private String memberExternalId;
    @NotNull
    private BigDecimal principal;
    @NotNull
    private BigDecimal interestRate;
    private int termMonths;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getMemberExternalId() {
        return memberExternalId;
    }

    public void setMemberExternalId(String memberExternalId) {
        this.memberExternalId = memberExternalId;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(int termMonths) {
        this.termMonths = termMonths;
    }
}
