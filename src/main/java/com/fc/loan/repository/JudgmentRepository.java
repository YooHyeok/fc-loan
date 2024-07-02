package com.fc.loan.repository;

import com.fc.loan.domain.Judgment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JudgmentRepository extends JpaRepository<Judgment, Long> {
    Optional<Judgment> findByApplicationId(Long applicationId);
}