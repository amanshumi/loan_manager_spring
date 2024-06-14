package com.amanshumi.loanmanager.repositories;
import com.amanshumi.loanmanager.entities.Borrower;
import com.amanshumi.loanmanager.entities.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    boolean existsByBorrowerAndApprovalStatus_Status(Borrower borrower, String status);
    @Query("SELECT la FROM LoanApplication la JOIN FETCH la.borrower WHERE la.id = :id")
    LoanApplication findByIdWithBorrower(Long id);
}
