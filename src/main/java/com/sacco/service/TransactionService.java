package com.sacco.service;

import com.sacco.config.Database;
import com.sacco.model.Role;
import com.sacco.model.TransactionRecord;
import com.sacco.repository.AuditRepository;
import com.sacco.repository.MemberRepository;
import com.sacco.repository.TransactionRepository;
import com.sacco.sync.OutboxRepository;
import com.sacco.util.JsonUtil;

import com.sacco.model.TransactionView;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public class TransactionService {
    private final TransactionRepository transactionRepository = new TransactionRepository();
    private final AuditRepository auditRepository = new AuditRepository();
    private final AuthorizationService authorizationService = new AuthorizationService();
    private final OutboxRepository outboxRepository = new OutboxRepository();
    private final MemberRepository memberRepository = new MemberRepository();

    public long recordTransaction(long actorUserId, long memberId, String type, BigDecimal amount) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String externalId = java.util.UUID.randomUUID().toString();
                long txId = transactionRepository.insert(connection, externalId, memberId, type, amount);
                String memberExternalId = memberRepository.findExternalId(connection, memberId);
                enqueueTransactionSync(connection, externalId, memberExternalId, type, amount);
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
        authorizationService.requireRole(actorUserId, "REVERSE_TX", "TRANSACTION", transactionId, Role.DIRECTOR);
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
                String externalId = java.util.UUID.randomUUID().toString();
                long reversalId = transactionRepository.insertReversal(connection, externalId, original.getMemberId(), transactionId, reversalAmount);
                String memberExternalId = memberRepository.findExternalId(connection, original.getMemberId());
                enqueueTransactionSync(connection, externalId, memberExternalId, "REVERSAL", reversalAmount);
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

    private void enqueueTransactionSync(Connection connection, String externalId, String memberExternalId, String type, BigDecimal amount) {
        String payload = "{" +
                "\"externalId\":\"" + JsonUtil.escape(externalId) + "\"," +
                "\"memberExternalId\":\"" + JsonUtil.escape(memberExternalId) + "\"," +
                "\"type\":\"" + JsonUtil.escape(type) + "\"," +
                "\"amount\":" + amount +
                "}";
        outboxRepository.enqueue(connection, "TRANSACTION_CREATE", payload);
    }
}
