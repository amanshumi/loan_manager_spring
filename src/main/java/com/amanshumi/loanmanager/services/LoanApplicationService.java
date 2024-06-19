package com.amanshumi.loanmanager.services;
import com.amanshumi.loanmanager.dto.request.ApproveLoanRequest;
import com.amanshumi.loanmanager.dto.request.LoanApplicationRequestDTO;
import com.amanshumi.loanmanager.dto.request.RejectLoanRequest;
import com.amanshumi.loanmanager.dto.request.RepaymentRequest;
import com.amanshumi.loanmanager.dto.response.*;
import com.amanshumi.loanmanager.entities.*;
import com.amanshumi.loanmanager.exceptions.*;
import com.amanshumi.loanmanager.repositories.BorrowerRepository;
import com.amanshumi.loanmanager.repositories.LoanApplicationRepository;
import com.amanshumi.loanmanager.repositories.LoanDisbursementRepository;
import com.amanshumi.loanmanager.repositories.RepaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LoanApplicationService {

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;
    @Autowired
    private LoanDisbursementRepository loanDisbursementRepository;
    @Autowired
    private RepaymentRepository repaymentRepository;
    @Autowired
    private BorrowerRepository borrowerRepository;

    private Logger myLoanLogger = LoggerFactory.getLogger(LoanApplicationService.class);

    @Transactional
    public LoanApplicationResponseDTO submitLoanApplication(LoanApplicationRequestDTO request) {
        myLoanLogger.info("Starting Loan Request");
        myLoanLogger.info("Payload: " + request.toString());
        // This validation can also be enhanced by defining rules on a separate entity on db, but I just put it here for demonstration
        validateIncomeToLoanRatio(request.getBorrowerIncome(), request.getLoanAmount());

        // Validate credit score
        validateCreditScore(request.getBorrowerCreditScore());

        // Check if borrower exists by email
        Optional<Borrower> optionalBorrower = borrowerRepository.findByEmail(request.getBorrowerEmail());
        Borrower borrower;

        if (optionalBorrower.isPresent()) {
            borrower = optionalBorrower.get();
            // Check for any outstanding loans
            boolean hasOutstandingLoan = loanApplicationRepository.existsByBorrowerAndApprovalStatus_Status(borrower, "PENDING") ||
                    loanApplicationRepository.existsByBorrowerAndApprovalStatus_Status(borrower, "APPROVED");
            if (hasOutstandingLoan) {
                myLoanLogger.error("Borrower has an outstanding loan");
                throw new OutstandingLoanException("Borrower has an outstanding loan. Please return the previous loan first.");
            }
        } else {
            // Create new borrower
            borrower = new Borrower();
            borrower.setName(request.getBorrowerName());
            borrower.setAddress(request.getBorrowerAddress());
            borrower.setPhoneNumber(request.getBorrowerPhoneNumber());
            borrower.setEmail(request.getBorrowerEmail());
            borrower.setIncome(request.getBorrowerIncome());
            // in a real world application, we should check the credit score based on the customer's previous profile or transaction history using a credit scoring engine. this is just for formality here
            borrower.setCreditScore(request.getBorrowerCreditScore());
            borrower = borrowerRepository.save(borrower);
        }

        // Create loan application
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setBorrower(borrower);
        loanApplication.setLoanAmount(request.getLoanAmount());
        loanApplication.setTerm(request.getTerm());
        loanApplication.setPurpose(request.getPurpose());
        loanApplication.setApplicationDate(LocalDate.now());

        // Set initial approval status
        ApprovalStatus approvalStatus = new ApprovalStatus();
        approvalStatus.setStatus("PENDING");
        approvalStatus.setLoanApplication(loanApplication);
        loanApplication.setApprovalStatus(approvalStatus);

        loanApplication = loanApplicationRepository.save(loanApplication);

        // Create response DTO
        LoanApplicationResponseDTO response = new LoanApplicationResponseDTO();
        response.setId(loanApplication.getId());
        response.setBorrowerId(borrower.getId());
        response.setLoanAmount(loanApplication.getLoanAmount());
        response.setTerm(loanApplication.getTerm());
        response.setPurpose(loanApplication.getPurpose());
        response.setStatus(approvalStatus.getStatus());
        response.setApplicationDate(loanApplication.getApplicationDate());

        myLoanLogger.info("Loan Created Successfully");
        myLoanLogger.info("DETAILS: " + response.toString());

        return response;
    }

    private void validateCreditScore(int creditScore) {
        if (creditScore < 580) {
            throw new LoanApplicationException("Credit score is too low for loan approval: " + creditScore);
        }
    }

    private void validateIncomeToLoanRatio(BigDecimal income, BigDecimal loanAmount) {
        BigDecimal ratio = income.divide(loanAmount, 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal minimumRatio = new BigDecimal("0.25"); // 1:4 ratio
        if (ratio.compareTo(minimumRatio) < 0) {
            throw new IncomeToLoanRatioException("Income to loan ratio is less than the required 1:4");
        }
    }

    public LoanDetailsResponseDTO getLoanDetailsById(Long loanId) {
        myLoanLogger.info("Starting fetching loan details by id: " + loanId);
        LoanApplication loanApplication = loanApplicationRepository.findByIdWithBorrower(loanId);
        if (loanApplication == null) {
            myLoanLogger.error("Loan application not found with id : " + loanId);
            throw new LoanApplicationNotFoundException("Loan application not found with id: " + loanId);
        }

        LoanDetailsResponseDTO responseDTO = new LoanDetailsResponseDTO();
        responseDTO.setId(loanApplication.getId());
        BorrowerDTO borrower = new BorrowerDTO();
        borrower.setId(loanApplication.getBorrower().getId());
        borrower.setName(loanApplication.getBorrower().getName());
        borrower.setEmail(loanApplication.getBorrower().getEmail());
        borrower.setPhoneNumber(loanApplication.getBorrower().getPhoneNumber());
        responseDTO.setBorrower(borrower);
        responseDTO.setLoanAmount(loanApplication.getLoanAmount());
        responseDTO.setTerm(loanApplication.getTerm());
        responseDTO.setPurpose(loanApplication.getPurpose());
        responseDTO.setStatus(loanApplication.getApprovalStatus().getStatus());
        responseDTO.setApplicationDate(loanApplication.getApplicationDate());

        myLoanLogger.info("Loan details fetched successfully");
        myLoanLogger.debug(responseDTO.toString());

        return responseDTO;
    }

    public ApproveLoanResponse approveLoanApplication(ApproveLoanRequest request) {
        myLoanLogger.info("Starting loan approval : " + request.toString());

        LoanApplication loanApplication = loanApplicationRepository.findById(request.getLoanId())
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found with id: " + request.getLoanId()));

        ApprovalStatus approvalStatus = loanApplication.getApprovalStatus();
        if (approvalStatus.getStatus().equals("APPROVED")) {
            myLoanLogger.error("Loan application already approved with id: " + request.getLoanId());
            throw new AlreadyApprovedException("Loan application with id " + request.getLoanId() + " is already approved");
        }

        approvalStatus.setStatus("APPROVED");
        approvalStatus.setReviewer(request.getApprover());
        approvalStatus.setApprovalDate(LocalDate.now());

        loanApplicationRepository.save(loanApplication);

        myLoanLogger.info("Loan application approved successfully : " + approvalStatus.toString());

        return new ApproveLoanResponse("Loan application with id " + request.getLoanId() + " has been approved", approvalStatus.getReviewer());
    }

    public RejectLoanResponse rejectLoan(RejectLoanRequest request) {
        myLoanLogger.info("Starting loan rejection : " + request.toString());
        LoanApplication loanApplication = loanApplicationRepository.findById(request.getLoanId())
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found with id: " + request.getLoanId()));

        ApprovalStatus approvalStatus = loanApplication.getApprovalStatus();

        if (approvalStatus.getStatus().equals("APPROVED")) {
            myLoanLogger.error("Cannot reject an already approved loan");
            throw new AlreadyApprovedException("Cannot reject an already approved loan");
        }

        if (approvalStatus.getStatus().equals("REJECTED")) {
            myLoanLogger.error("Loan with id " + request.getLoanId() + " is already rejected");
            throw new AlreadyApprovedException("Loan application with id " + request.getLoanId() + " is already rejected");
        }

        approvalStatus.setStatus("REJECTED");
        approvalStatus.setReviewer(request.getApprover());
        approvalStatus.setMessage(request.getReason());
        approvalStatus.setApprovalDate(LocalDate.now());

        loanApplicationRepository.save(loanApplication);

        myLoanLogger.info("Loan rejection successful : " + approvalStatus.toString());

        return new RejectLoanResponse("Loan application with id " + request.getLoanId() + " has been rejected", approvalStatus.getMessage());
    }


}
