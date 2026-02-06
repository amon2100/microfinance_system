package com.sacco.backend.controller;

import com.sacco.backend.dto.PaymentCallbackRequest;
import com.sacco.backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    private final PaymentService paymentService;

    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{provider}")
    public void handle(@PathVariable String provider,
                       @RequestHeader(name = "X-Signature", required = false) String signature,
                       @Valid @RequestBody PaymentCallbackRequest request) {
        paymentService.handleCallback(provider.toUpperCase(), signature, request);
    }
}
