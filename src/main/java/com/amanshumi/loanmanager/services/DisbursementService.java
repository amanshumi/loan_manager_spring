package com.amanshumi.loanmanager.services;

import com.amanshumi.loanmanager.dto.response.DisburseLoanResponse;
import com.amanshumi.loanmanager.entities.ApprovalStatus;
import com.amanshumi.loanmanager.entities.LoanApplication;
import com.amanshumi.loanmanager.entities.LoanDisbursement;
import com.amanshumi.loanmanager.exceptions.LoanAlreadyDisbursedException;
import com.amanshumi.loanmanager.exceptions.LoanApplicationNotFoundException;
import com.amanshumi.loanmanager.exceptions.LoanNotApprovedException;
import com.amanshumi.loanmanager.repositories.BorrowerRepository;
import com.amanshumi.loanmanager.repositories.LoanApplicationRepository;
import com.amanshumi.loanmanager.repositories.LoanDisbursementRepository;
import com.amanshumi.loanmanager.repositories.RepaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DisbursementService {
    @Autowired
    private LoanApplicationRepository loanApplicationRepository;
    @Autowired
    private LoanDisbursementRepository loanDisbursementRepository;
    @Autowired
    private RepaymentRepository repaymentRepository;
    @Autowired
    private BorrowerRepository borrowerRepository;

    public DisburseLoanResponse disburseLoan(Long loanId) {
        LoanApplication loanApplication = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found with id: " + loanId));

        ApprovalStatus approvalStatus = loanApplication.getApprovalStatus();
        if (!approvalStatus.getStatus().equals("APPROVED")) {
            throw new LoanNotApprovedException("Loan application with id " + loanId + " is not approved");
        }

        LoanDisbursement existingDisbursement = loanDisbursementRepository.findByLoanApplication(loanApplication);
        if (existingDisbursement != null) {
            throw new LoanAlreadyDisbursedException("Loan application with id " + loanId + " is already disbursed");
        }

        LoanDisbursement loanDisbursement = new LoanDisbursement();
        loanDisbursement.setLoanApplication(loanApplication);
        loanDisbursement.setDisbursementDate(LocalDate.now());
        // Set the disbursed amount as per your business logic
        loanDisbursement.setDisbursedAmount(loanApplication.getLoanAmount()); // Example amount

        loanDisbursementRepository.save(loanDisbursement);

        return new DisburseLoanResponse("Loan application with id " + loanId + " has been disbursed with amount: " + loanApplication.getLoanAmount());
    }
}
