package com.sacco.backend.repository;

import com.sacco.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByReversedOf(Long reversedOf);
    Optional<Transaction> findByExternalId(String externalId);
}
