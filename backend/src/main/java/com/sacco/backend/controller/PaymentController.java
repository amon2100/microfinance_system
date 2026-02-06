package com.sacco.backend.controller;

import com.sacco.backend.dto.RepaymentRequest;
import com.sacco.backend.model.Payment;
import com.sacco.backend.security.AuthUser;
import com.sacco.backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/repayments")
    public Payment initiateRepayment(@Valid @RequestBody RepaymentRequest request, Authentication authentication) {
        AuthUser actor = (AuthUser) authentication.getPrincipal();
        return paymentService.initiateRepayment(request, actor.userId());
    }
}
