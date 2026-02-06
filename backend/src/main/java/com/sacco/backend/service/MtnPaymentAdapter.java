package com.sacco.backend.service;

import com.sacco.backend.model.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MtnPaymentAdapter implements PaymentAdapter {
    private final String callbackSecret;

    public MtnPaymentAdapter(@Value("${app.payments.mtn.callbackSecret}") String callbackSecret) {
        this.callbackSecret = callbackSecret;
    }

    @Override
    public PaymentInitiationResult initiatePayment(Payment payment, String phoneNumber) {
        // TODO: Call MTN MoMo API with phoneNumber, amount, idempotencyKey.
        String providerReference = "MTN-" + payment.getIdempotencyKey();
        return new PaymentInitiationResult(providerReference, "PENDING");
    }

    @Override
    public boolean verifyCallback(String signatureHeader, String rawBody) {
        // TODO: Validate MTN signature; placeholder uses shared secret.
        return signatureHeader != null && signatureHeader.equals(callbackSecret);
    }
}
