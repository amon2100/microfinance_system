package com.sacco.backend.service;

import com.sacco.backend.model.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AirtelPaymentAdapter implements PaymentAdapter {
    private final String callbackSecret;

    public AirtelPaymentAdapter(@Value("${app.payments.airtel.callbackSecret}") String callbackSecret) {
        this.callbackSecret = callbackSecret;
    }

    @Override
    public PaymentInitiationResult initiatePayment(Payment payment, String phoneNumber) {
        // TODO: Call Airtel Money API with phoneNumber, amount, idempotencyKey.
        String providerReference = "AIRTEL-" + payment.getIdempotencyKey();
        return new PaymentInitiationResult(providerReference, "PENDING");
    }

    @Override
    public boolean verifyCallback(String signatureHeader, String rawBody) {
        // TODO: Validate Airtel signature; placeholder uses shared secret.
        return signatureHeader != null && signatureHeader.equals(callbackSecret);
    }
}
