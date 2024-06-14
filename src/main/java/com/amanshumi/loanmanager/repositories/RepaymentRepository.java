package com.amanshumi.loanmanager.repositories;

import com.amanshumi.loanmanager.entities.LoanApplication;
import com.amanshumi.loanmanager.entities.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Repayment r WHERE r.loanApplication = :loanApplication")
    BigDecimal getTotalRepaymentsByLoanApplication(@Param("loanApplication") LoanApplication loanApplication);

    List<Repayment> findByLoanApplicationId(Long loanApplicationId);
}

