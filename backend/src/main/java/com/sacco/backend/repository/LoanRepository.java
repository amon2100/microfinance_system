package com.sacco.backend.repository;

import com.sacco.backend.model.Loan;
import com.sacco.backend.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    boolean existsByMemberIdAndStatus(Long memberId, LoanStatus status);
}
