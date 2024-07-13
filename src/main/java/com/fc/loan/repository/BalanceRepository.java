package com.fc.loan.repository;

import com.fc.loan.domain.Balance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BalanceRepository extends JpaRepository<Balance, Long> {
    Optional<Balance> findByApplicationId(Long applicationId);
}