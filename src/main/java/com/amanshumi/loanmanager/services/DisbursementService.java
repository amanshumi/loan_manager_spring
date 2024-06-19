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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private Logger myLoanLogger = LoggerFactory.getLogger(DisbursementService.class);

    public DisburseLoanResponse disburseLoan(Long loanId) {
        myLoanLogger.info("Starting Loan Disbursement Request for loan id : " + loanId);
        LoanApplication loanApplication = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found with id: " + loanId));

        ApprovalStatus approvalStatus = loanApplication.getApprovalStatus();
        if (!approvalStatus.getStatus().equals("APPROVED")) {
            myLoanLogger.error("Loan with id : " + loanId + " is not approved. Can't disburse an unapproved loan");
            throw new LoanNotApprovedException("Loan application with id " + loanId + " is not approved");
        }

        LoanDisbursement existingDisbursement = loanDisbursementRepository.findByLoanApplication(loanApplication);
        if (existingDisbursement != null) {
            myLoanLogger.error("Loan application with id : " + loanId + " is already disbursed");
            throw new LoanAlreadyDisbursedException("Loan application with id " + loanId + " is already disbursed");
        }

        LoanDisbursement loanDisbursement = new LoanDisbursement();
        loanDisbursement.setLoanApplication(loanApplication);
        loanDisbursement.setDisbursementDate(LocalDate.now());
        // Set the disbursed amount as per your business logic
        loanDisbursement.setDisbursedAmount(loanApplication.getLoanAmount()); // Example amount

        loanDisbursementRepository.save(loanDisbursement);

        myLoanLogger.info("Loan disbursement for loan id : " + loanId + " is successfully completed");

        return new DisburseLoanResponse("Loan application with id " + loanId + " has been disbursed with amount: " + loanApplication.getLoanAmount());
    }
}
