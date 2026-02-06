package com.sacco.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class SavingsSyncRequest {
    @NotBlank
    private String externalId;
    @NotBlank
    private String memberExternalId;
    @NotBlank
    private String accountNo;

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

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }
}
