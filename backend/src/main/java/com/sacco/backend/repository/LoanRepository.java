package com.sacco.backend.repository;

import com.sacco.backend.model.Loan;
import com.sacco.backend.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    boolean existsByMemberIdAndStatus(Long memberId, LoanStatus status);
    Optional<Loan> findByExternalId(String externalId);
}
