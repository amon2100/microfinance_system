package com.sacco.service;

import com.sacco.config.Database;
import com.sacco.model.Role;
import com.sacco.model.TransactionRecord;
import com.sacco.repository.AuditRepository;
import com.sacco.repository.TransactionRepository;

import com.sacco.model.TransactionView;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public class TransactionService {
    private final TransactionRepository transactionRepository = new TransactionRepository();
    private final AuditRepository auditRepository = new AuditRepository();
    private final AuthorizationService authorizationService = new AuthorizationService();

    public long recordTransaction(long actorUserId, long memberId, String type, BigDecimal amount) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long txId = transactionRepository.insert(connection, memberId, type, amount);
                auditRepository.insert(connection, actorUserId, "TX_CREATED", "TRANSACTION", txId,
                        "Type=" + type + ", Amount=" + amount);
                connection.commit();
                return txId;
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Transaction recording failed", ex);
        }
    }

    public List<TransactionView> listTransactions() {
        try (Connection connection = Database.getConnection()) {
            return transactionRepository.findAll(connection);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load transactions", ex);
        }
    }

    public long reverseTransaction(long actorUserId, long transactionId) {
        authorizationService.requireRole(actorUserId, "REVERSE_TX", "TRANSACTION", transactionId, Role.ADMIN);
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                TransactionRecord original = transactionRepository.findById(connection, transactionId);
                if (original == null) {
                    throw new ValidationException("Transaction not found");
                }
                if (original.getReversedOf() != null) {
                    throw new ValidationException("Cannot reverse a reversal transaction");
                }
                if (transactionRepository.hasReversal(connection, transactionId)) {
                    throw new ValidationException("Transaction already reversed");
                }
                BigDecimal reversalAmount = original.getAmount().negate();
                long reversalId = transactionRepository.insertReversal(connection, original.getMemberId(), transactionId, reversalAmount);
                auditRepository.insert(connection, actorUserId, "TX_REVERSED", "TRANSACTION", reversalId,
                        "OriginalTxId=" + transactionId + ", Amount=" + reversalAmount);
                connection.commit();
                return reversalId;
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Transaction reversal failed", ex);
        }
    }
}
