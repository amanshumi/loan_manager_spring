package com.amanshumi.loanmanager.repositories;
import com.amanshumi.loanmanager.entities.LoanApplication;
import com.amanshumi.loanmanager.entities.LoanDisbursement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanDisbursementRepository extends JpaRepository<LoanDisbursement, Long> {
    LoanDisbursement findByLoanApplication(LoanApplication loanApplication);
}
