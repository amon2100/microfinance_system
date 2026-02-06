package com.sacco.backend.service;

import com.sacco.backend.dto.PaymentCallbackRequest;
import com.sacco.backend.dto.RepaymentRequest;
import com.sacco.backend.model.*;
import com.sacco.backend.repository.LoanRepository;
import com.sacco.backend.repository.PaymentRepository;
import com.sacco.backend.repository.TransactionRepository;
import com.sacco.backend.security.AuthUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final LoanRepository loanRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;
    private final MtnPaymentAdapter mtnPaymentAdapter;
    private final AirtelPaymentAdapter airtelPaymentAdapter;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              LoanRepository loanRepository,
                              TransactionRepository transactionRepository,
                              AuditService auditService,
                              MtnPaymentAdapter mtnPaymentAdapter,
                              AirtelPaymentAdapter airtelPaymentAdapter) {
        this.paymentRepository = paymentRepository;
        this.loanRepository = loanRepository;
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
        this.mtnPaymentAdapter = mtnPaymentAdapter;
        this.airtelPaymentAdapter = airtelPaymentAdapter;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TELLER')")
    public Payment initiateRepayment(RepaymentRequest request, Long actorUserId) {
        validateRepaymentRequest(request);
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new ValidationException("Loan not found"));
        if (loan.getStatus() != LoanStatus.DISBURSED) {
            throw new ValidationException("Loan must be DISBURSED");
        }
        if (request.getAmount().compareTo(loan.getOutstandingBalance()) > 0) {
            throw new ValidationException("Repayment exceeds outstanding balance");
        }

        Payment existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey()).orElse(null);
        if (existing != null) {
            return existing;
        }

        Payment payment = new Payment();
        payment.setLoanId(request.getLoanId());
        payment.setMemberId(request.getMemberId());
        payment.setAmount(request.getAmount());
        payment.setIdempotencyKey(request.getIdempotencyKey());
        payment.setProvider(PaymentProvider.valueOf(request.getProvider()));
        payment = paymentRepository.save(payment);

        PaymentAdapter adapter = resolveAdapter(payment.getProvider());
        PaymentInitiationResult result = adapter.initiatePayment(payment, "TODO_PHONE_NUMBER");
        payment.setProviderReference(result.providerReference());
        paymentRepository.save(payment);

        auditService.log(actorUserId, "PAYMENT_INITIATED", "PAYMENT", payment.getId(),
                "LoanId=" + payment.getLoanId() + ", Amount=" + payment.getAmount());
        return payment;
    }

    @Override
    @Transactional
    public void handleCallback(String provider, String signature, PaymentCallbackRequest callbackRequest) {
        PaymentProvider paymentProvider = PaymentProvider.valueOf(provider);
        PaymentAdapter adapter = resolveAdapter(paymentProvider);
        if (!adapter.verifyCallback(signature, callbackRequest.getRawPayload())) {
            auditService.log(null, "PAYMENT_CALLBACK_REJECTED", "PAYMENT", null, "Invalid signature");
            throw new ValidationException("Invalid callback signature");
        }

        Payment payment = paymentRepository.findByProviderReference(callbackRequest.getProviderReference())
                .orElseThrow(() -> new ValidationException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return; // idempotent
        }

        if ("SUCCESS".equalsIgnoreCase(callbackRequest.getStatus())) {
            applySuccessfulRepayment(payment, callbackRequest);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setProviderPayload(callbackRequest.getRawPayload());
            paymentRepository.save(payment);
            auditService.log(null, "PAYMENT_FAILED", "PAYMENT", payment.getId(), "Provider status: " + callbackRequest.getStatus());
        }
    }

    private void applySuccessfulRepayment(Payment payment, PaymentCallbackRequest callbackRequest) {
        Loan loan = loanRepository.findById(payment.getLoanId())
                .orElseThrow(() -> new ValidationException("Loan not found"));

        if (payment.getAmount().compareTo(loan.getOutstandingBalance()) > 0) {
            throw new ValidationException("Repayment exceeds outstanding balance");
        }

        Transaction tx = new Transaction();
        tx.setMemberId(payment.getMemberId());
        tx.setType("LOAN_REPAYMENT");
        tx.setAmount(payment.getAmount());
        transactionRepository.save(tx);

        BigDecimal newBalance = loan.getOutstandingBalance().subtract(payment.getAmount());
        loan.setOutstandingBalance(newBalance);
        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(LoanStatus.CLOSED);
        }
        loanRepository.save(loan);

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setProviderPayload(callbackRequest.getRawPayload());
        paymentRepository.save(payment);

        auditService.log(null, "PAYMENT_SUCCESS", "PAYMENT", payment.getId(), "Applied repayment");
    }

    private PaymentAdapter resolveAdapter(PaymentProvider provider) {
        return switch (provider) {
            case MTN_MOMO -> mtnPaymentAdapter;
            case AIRTEL_MONEY -> airtelPaymentAdapter;
        };
    }

    private void validateRepaymentRequest(RepaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Repayment amount must be > 0");
        }
        if (request.getProvider() == null || request.getProvider().isBlank()) {
            throw new ValidationException("Provider required");
        }
    }
}
