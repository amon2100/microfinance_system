package com.sacco.backend.service;

import com.sacco.backend.model.Payment;

public interface PaymentAdapter {
    PaymentInitiationResult initiatePayment(Payment payment, String phoneNumber);
    boolean verifyCallback(String signatureHeader, String rawBody);
}
