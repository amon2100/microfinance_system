package com.sacco.backend.repository;

import com.sacco.backend.model.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long> {
    Optional<SavingsAccount> findByExternalId(String externalId);
}
