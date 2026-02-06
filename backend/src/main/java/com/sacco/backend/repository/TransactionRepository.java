package com.sacco.backend.repository;

import com.sacco.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByReversedOf(Long reversedOf);
}
