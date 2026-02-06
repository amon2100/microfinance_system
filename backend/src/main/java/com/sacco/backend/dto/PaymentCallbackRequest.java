package com.sacco.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class PaymentCallbackRequest {
    @NotBlank
    private String providerReference;

    @NotBlank
    private String status;

    @NotBlank
    private String rawPayload;

    public String getProviderReference() {
        return providerReference;
    }

    public void setProviderReference(String providerReference) {
        this.providerReference = providerReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }
}
