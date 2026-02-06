package com.sacco.backend.service;

import com.sacco.backend.dto.PaymentCallbackRequest;
import com.sacco.backend.dto.RepaymentRequest;
import com.sacco.backend.model.Payment;

public interface PaymentService {
    Payment initiateRepayment(RepaymentRequest request, Long actorUserId);
    void handleCallback(String provider, String signature, PaymentCallbackRequest callbackRequest);
}
