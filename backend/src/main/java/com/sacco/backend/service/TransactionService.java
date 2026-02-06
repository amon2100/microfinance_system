package com.sacco.backend.service;

import com.sacco.backend.model.Transaction;
import com.sacco.backend.repository.TransactionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    public TransactionService(TransactionRepository transactionRepository, AuditService auditService) {
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Transaction reverseTransaction(Long transactionId, Long actorUserId) {
        Transaction original = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ValidationException("Transaction not found"));
        if (original.getReversedOf() != null) {
            throw new ValidationException("Cannot reverse a reversal transaction");
        }
        if (transactionRepository.existsByReversedOf(transactionId)) {
            throw new ValidationException("Transaction already reversed");
        }
        Transaction reversal = new Transaction();
        reversal.setMemberId(original.getMemberId());
        reversal.setType("REVERSAL");
        reversal.setAmount(original.getAmount().negate());
        reversal.setReversedOf(transactionId);
        Transaction saved = transactionRepository.save(reversal);
        auditService.log(actorUserId, "TX_REVERSED", "TRANSACTION", saved.getId(), "OriginalTxId=" + transactionId);
        return saved;
    }

    public void validateDeposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Deposit amount must be > 0");
        }
    }

    public void validateWithdrawal(BigDecimal amount, BigDecimal currentBalance) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Withdrawal amount must be > 0");
        }
        if (currentBalance.compareTo(amount) < 0) {
            throw new ValidationException("Insufficient balance");
        }
    }
}
