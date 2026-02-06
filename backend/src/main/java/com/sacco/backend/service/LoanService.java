package com.sacco.backend.service;

import com.sacco.backend.dto.LoanCreateRequest;
import com.sacco.backend.model.Loan;
import com.sacco.backend.model.LoanStatus;
import com.sacco.backend.model.Role;
import com.sacco.backend.repository.LoanRepository;
import com.sacco.backend.repository.MemberRepository;
import com.sacco.backend.security.AuthUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class LoanService {
    private final LoanRepository loanRepository;
    private final MemberRepository memberRepository;
    private final AuditService auditService;

    public LoanService(LoanRepository loanRepository, MemberRepository memberRepository, AuditService auditService) {
        this.loanRepository = loanRepository;
        this.memberRepository = memberRepository;
        this.auditService = auditService;
    }

    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public Loan createLoan(LoanCreateRequest request, AuthUser actor) {
        validateLoanRequest(request);
        if (!memberRepository.existsById(request.getMemberId())) {
            throw new ValidationException("Member not found");
        }
        if (loanRepository.existsByMemberIdAndStatus(request.getMemberId(), LoanStatus.DEFAULTED)) {
            throw new ValidationException("Member has defaulted loans");
        }
        Loan loan = new Loan();
        loan.setMemberId(request.getMemberId());
        loan.setPrincipal(request.getPrincipal());
        loan.setInterestRate(request.getInterestRate());
        loan.setTermMonths(request.getTermMonths());
        loan.setStatus(LoanStatus.PENDING);
        loan.setCreatedBy(actor.userId());
        loan.setOutstandingBalance(request.getPrincipal());
        Loan saved = loanRepository.save(loan);
        auditService.log(actor.userId(), "LOAN_CREATED", "LOAN", saved.getId(), "Created loan");
        return saved;
    }

    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public void approveLoan(Long loanId, AuthUser actor) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ValidationException("Loan not found"));
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new ValidationException("Loan must be PENDING");
        }
        if (loan.getCreatedBy() != null && loan.getCreatedBy().equals(actor.userId())) {
            throw new ValidationException("Approval must be by a different user");
        }
        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedBy(actor.userId());
        loanRepository.save(loan);
        auditService.log(actor.userId(), "LOAN_APPROVED", "LOAN", loanId, "Approved loan");
    }

    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public void disburseLoan(Long loanId, AuthUser actor) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ValidationException("Loan not found"));
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new ValidationException("Loan must be APPROVED before disbursement");
        }
        loan.setStatus(LoanStatus.DISBURSED);
        loan.setDisbursedBy(actor.userId());
        loanRepository.save(loan);
        auditService.log(actor.userId(), "LOAN_DISBURSED", "LOAN", loanId, "Disbursed loan");
    }

    private void validateLoanRequest(LoanCreateRequest request) {
        if (request.getPrincipal() == null || request.getPrincipal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Loan amount must be > 0");
        }
        if (request.getTermMonths() < 1 || request.getTermMonths() > 36) {
            throw new ValidationException("Loan term must be between 1 and 36 months");
        }
        if (request.getInterestRate() == null
                || request.getInterestRate().compareTo(new BigDecimal("0.01")) < 0
                || request.getInterestRate().compareTo(new BigDecimal("0.30")) > 0) {
            throw new ValidationException("Interest rate must be between 1% and 30%");
        }
    }
}
