package com.sacco.backend.dto;

import jakarta.validation.constraints.NotNull;

public class LoanActionRequest {
    @NotNull
    private Long loanId;

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }
}
