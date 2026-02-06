package com.sacco.backend.repository;

import com.sacco.backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findByProviderReference(String providerReference);
}
