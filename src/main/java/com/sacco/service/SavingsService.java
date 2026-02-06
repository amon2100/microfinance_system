package com.sacco.service;

import com.sacco.config.Database;
import com.sacco.model.Role;
import com.sacco.model.SavingsAccount;
import com.sacco.model.User;
import com.sacco.repository.AuditRepository;
import com.sacco.repository.SavingsRepository;
import com.sacco.repository.TransactionRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public class SavingsService {
    private final SavingsRepository savingsRepository = new SavingsRepository();
    private final TransactionRepository transactionRepository = new TransactionRepository();
    private final AuditRepository auditRepository = new AuditRepository();
    private final AuthorizationService authorizationService = new AuthorizationService();

    public long createAccount(long actorUserId, long memberId, String accountNo) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long accountId = savingsRepository.createAccount(connection, memberId, accountNo);
                auditRepository.insert(connection, actorUserId, "SAVINGS_CREATED", "SAVINGS_ACCOUNT", accountId,
                        "AccountNo=" + accountNo);
                connection.commit();
                return accountId;
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Savings account creation failed", ex);
        }
    }

    public void deposit(long actorUserId, String accountNo, BigDecimal amount) {
        User actor = authorizationService.requireRole(actorUserId, "SAVINGS_DEPOSIT", "SAVINGS_ACCOUNT", null, Role.TELLER);
        if (amount.signum() <= 0) {
            throw new ValidationException("Amount must be positive");
        }
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal balance = savingsRepository.getBalance(connection, accountNo);
                if (balance == null) {
                    throw new ValidationException("Account not found");
                }
                long accountMemberId = savingsRepository.getMemberId(connection, accountNo);
                if (actor.getMemberId() != null && actor.getMemberId() == accountMemberId) {
                    throw new ValidationException("Teller cannot transact on own account");
                }
                BigDecimal newBalance = balance.add(amount);
                savingsRepository.updateBalance(connection, accountNo, newBalance);
                long memberId = accountMemberId;
                long txId = transactionRepository.insert(connection, memberId, "SAVINGS_DEPOSIT", amount);
                auditRepository.insert(connection, actorUserId, "SAVINGS_DEPOSIT", "TRANSACTION", txId,
                        "AccountNo=" + accountNo + ", Amount=" + amount);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Deposit failed", ex);
        }
    }

    public void withdraw(long actorUserId, String accountNo, BigDecimal amount) {
        User actor = authorizationService.requireRole(actorUserId, "SAVINGS_WITHDRAWAL", "SAVINGS_ACCOUNT", null, Role.TELLER);
        if (amount.signum() <= 0) {
            throw new ValidationException("Amount must be positive");
        }
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal balance = savingsRepository.getBalance(connection, accountNo);
                if (balance == null) {
                    throw new ValidationException("Account not found");
                }
                if (balance.compareTo(amount) < 0) {
                    throw new ValidationException("Insufficient funds");
                }
                long accountMemberId = savingsRepository.getMemberId(connection, accountNo);
                if (actor.getMemberId() != null && actor.getMemberId() == accountMemberId) {
                    throw new ValidationException("Teller cannot transact on own account");
                }
                BigDecimal newBalance = balance.subtract(amount);
                savingsRepository.updateBalance(connection, accountNo, newBalance);
                long memberId = accountMemberId;
                long txId = transactionRepository.insert(connection, memberId, "SAVINGS_WITHDRAWAL", amount);
                auditRepository.insert(connection, actorUserId, "SAVINGS_WITHDRAWAL", "TRANSACTION", txId,
                        "AccountNo=" + accountNo + ", Amount=" + amount);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Withdrawal failed", ex);
        }
    }

    public List<SavingsAccount> listAccounts() {
        try (Connection connection = Database.getConnection()) {
            return savingsRepository.findAll(connection);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load savings accounts", ex);
        }
    }
}
