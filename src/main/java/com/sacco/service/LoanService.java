package com.sacco.service;

import com.sacco.config.Database;
import com.sacco.model.Loan;
import com.sacco.model.Role;
import com.sacco.model.User;
import com.sacco.repository.AuditRepository;
import com.sacco.repository.LoanRepaymentRepository;
import com.sacco.repository.LoanRepository;
import com.sacco.repository.MemberRepository;
import com.sacco.repository.TransactionRepository;
import com.sacco.sync.OutboxRepository;
import com.sacco.util.JsonUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public class LoanService {
    private final LoanRepository loanRepository = new LoanRepository();
    private final LoanRepaymentRepository loanRepaymentRepository = new LoanRepaymentRepository();
    private final TransactionRepository transactionRepository = new TransactionRepository();
    private final AuditRepository auditRepository = new AuditRepository();
    private final MemberRepository memberRepository = new MemberRepository();
    private final AuthorizationService authorizationService = new AuthorizationService();
    private final OutboxRepository outboxRepository = new OutboxRepository();

    public long issueLoan(long actorUserId, long memberId, BigDecimal principal, BigDecimal interestRate, int termMonths) {
        User actor = authorizationService.requireRole(actorUserId, "LOAN_CREATE", "LOAN", null, Role.MANAGER);
        if (principal.signum() <= 0) {
            throw new ValidationException("Principal must be positive");
        }
        if (termMonths < 1 || termMonths > 36) {
            throw new ValidationException("Loan term must be between 1 and 36 months");
        }
        if (interestRate.compareTo(new BigDecimal("0.01")) < 0 || interestRate.compareTo(new BigDecimal("0.30")) > 0) {
            throw new ValidationException("Interest rate must be between 1% and 30%");
        }
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!memberRepository.existsActiveById(connection, memberId)) {
                    throw new ValidationException("Member not found or inactive");
                }
                if (loanRepository.hasDefaultedLoans(connection, memberId)) {
                    throw new ValidationException("Member has defaulted loans");
                }
                String externalId = java.util.UUID.randomUUID().toString();
                long loanId = loanRepository.create(connection, externalId, memberId, principal, interestRate, termMonths, actor.getId());
                String memberExternalId = memberRepository.findExternalId(connection, memberId);
                enqueueLoanSync(connection, externalId, memberExternalId, principal, interestRate, termMonths);
                auditRepository.insert(connection, actorUserId, "LOAN_CREATED", "LOAN", loanId,
                        "Principal=" + principal + ", Rate=" + interestRate + ", Term=" + termMonths);
                connection.commit();
                return loanId;
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Loan issuance failed", ex);
        }
    }

    public void approveLoan(long actorUserId, long loanId) {
        User actor = authorizationService.requireRole(actorUserId, "LOAN_APPROVE", "LOAN", loanId, Role.MANAGER);
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Loan loan = loanRepository.findById(connection, loanId);
                if (loan == null) {
                    throw new ValidationException("Loan not found");
                }
                if (!"PENDING".equalsIgnoreCase(loan.getStatus())) {
                    throw new ValidationException("Loan must be in PENDING status");
                }
                if (loan.getCreatedBy() != null && loan.getCreatedBy().equals(actor.getId())) {
                    throw new ValidationException("Loan approval must be done by a different user");
                }
                loanRepository.updateStatus(connection, loanId, "APPROVED", actor.getId(), null);
                auditRepository.insert(connection, actorUserId, "LOAN_APPROVED", "LOAN", loanId, "Approved loan");
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Loan approval failed", ex);
        }
    }

    public void disburseLoan(long actorUserId, long loanId) {
        authorizationService.requireRole(actorUserId, "LOAN_DISBURSE", "LOAN", loanId, Role.MANAGER);
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Loan loan = loanRepository.findById(connection, loanId);
                if (loan == null) {
                    throw new ValidationException("Loan not found");
                }
                if (!"APPROVED".equalsIgnoreCase(loan.getStatus())) {
                    throw new ValidationException("Loan must be APPROVED before disbursement");
                }
                loanRepository.updateStatus(connection, loanId, "DISBURSED", null, actorUserId);
                auditRepository.insert(connection, actorUserId, "LOAN_DISBURSED", "LOAN", loanId, "Disbursed loan");
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Loan disbursement failed", ex);
        }
    }

    public void recordRepayment(long actorUserId, long loanId, long memberId, BigDecimal amount) {
        authorizationService.requireRole(actorUserId, "LOAN_REPAY", "LOAN", loanId, Role.TELLER);
        if (amount.signum() <= 0) {
            throw new ValidationException("Amount must be positive");
        }
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Loan loan = loanRepository.findById(connection, loanId);
                if (loan == null) {
                    throw new ValidationException("Loan not found");
                }
                if (!"DISBURSED".equalsIgnoreCase(loan.getStatus())) {
                    throw new ValidationException("Loan must be DISBURSED to accept repayments");
                }
                if (amount.compareTo(loan.getOutstandingBalance()) > 0) {
                    throw new ValidationException("Repayment cannot exceed outstanding balance");
                }
                long repaymentId = loanRepaymentRepository.insert(connection, loanId, amount);
                String externalId = java.util.UUID.randomUUID().toString();
                long txId = transactionRepository.insert(connection, externalId, memberId, "LOAN_REPAYMENT", amount);
                String memberExternalId = memberRepository.findExternalId(connection, memberId);
                enqueueTransactionSync(connection, externalId, memberExternalId, "LOAN_REPAYMENT", amount);
                BigDecimal newBalance = loan.getOutstandingBalance().subtract(amount);
                loanRepository.updateOutstandingBalance(connection, loanId, newBalance);
                if (newBalance.signum() == 0) {
                    loanRepository.updateStatus(connection, loanId, "CLOSED", null, null);
                }
                auditRepository.insert(connection, actorUserId, "LOAN_REPAYMENT", "LOAN_REPAYMENT", repaymentId,
                        "LoanId=" + loanId + ", Amount=" + amount + ", TxId=" + txId);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Loan repayment failed", ex);
        }
    }

    public List<Loan> listLoans() {
        try (Connection connection = Database.getConnection()) {
            return loanRepository.findAll(connection);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load loans", ex);
        }
    }

    private void enqueueLoanSync(Connection connection, String externalId, String memberExternalId,
                                 BigDecimal principal, BigDecimal interestRate, int termMonths) {
        String payload = "{" +
                "\"externalId\":\"" + JsonUtil.escape(externalId) + "\"," +
                "\"memberExternalId\":\"" + JsonUtil.escape(memberExternalId) + "\"," +
                "\"principal\":" + principal + "," +
                "\"interestRate\":" + interestRate + "," +
                "\"termMonths\":" + termMonths +
                "}";
        outboxRepository.enqueue(connection, "LOAN_CREATE", payload);
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
