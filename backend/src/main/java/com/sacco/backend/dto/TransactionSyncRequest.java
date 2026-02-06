package com.sacco.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransactionSyncRequest {
    @NotBlank
    private String externalId;
    @NotBlank
    private String memberExternalId;
    @NotBlank
    private String type;
    @NotNull
    private BigDecimal amount;

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
}
