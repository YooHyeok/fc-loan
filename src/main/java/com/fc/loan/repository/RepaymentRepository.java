package com.fc.loan.repository;

import com.fc.loan.domain.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {

}
