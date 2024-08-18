package com.fc.loan.repository;

import com.fc.loan.domain.Repayment;
import com.fc.loan.dto.RepaymentDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {

    List<Repayment> findAllByApplicationId(Long applicationId);
}
