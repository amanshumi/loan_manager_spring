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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Transactional
    public LoanApplicationResponseDTO submitLoanApplication(LoanApplicationRequestDTO request) {
        // Check if borrower exists by email
        Optional<Borrower> optionalBorrower = borrowerRepository.findByEmail(request.getBorrowerEmail());
        Borrower borrower;
        if (optionalBorrower.isPresent()) {
            borrower = optionalBorrower.get();
            // Check for any outstanding loans
            boolean hasOutstandingLoan = loanApplicationRepository.existsByBorrowerAndApprovalStatus_Status(borrower, "PENDING") ||
                    loanApplicationRepository.existsByBorrowerAndApprovalStatus_Status(borrower, "APPROVED");
            if (hasOutstandingLoan) {
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

        return response;
    }

    public LoanDetailsResponseDTO getLoanDetailsById(Long loanId) {
        LoanApplication loanApplication = loanApplicationRepository.findByIdWithBorrower(loanId);
        if (loanApplication == null) {
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

        return responseDTO;
    }

    public ApproveLoanResponse approveLoanApplication(ApproveLoanRequest request) {
        LoanApplication loanApplication = loanApplicationRepository.findById(request.getLoanId())
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found with id: " + request.getLoanId()));

        ApprovalStatus approvalStatus = loanApplication.getApprovalStatus();
        if (approvalStatus.getStatus().equals("APPROVED")) {
            throw new AlreadyApprovedException("Loan application with id " + request.getLoanId() + " is already approved");
        }

        approvalStatus.setStatus("APPROVED");
        approvalStatus.setReviewer(request.getApprover());
        approvalStatus.setApprovalDate(LocalDate.now());

        loanApplicationRepository.save(loanApplication);

        return new ApproveLoanResponse("Loan application with id " + request.getLoanId() + " has been approved", approvalStatus.getReviewer());
    }

    public RejectLoanResponse rejectLoan(RejectLoanRequest request) {
        LoanApplication loanApplication = loanApplicationRepository.findById(request.getLoanId())
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found with id: " + request.getLoanId()));

        ApprovalStatus approvalStatus = loanApplication.getApprovalStatus();

        if (approvalStatus.getStatus().equals("APPROVED")) {
            throw new AlreadyApprovedException("Cannot reject an already approved loan");
        }

        if (approvalStatus.getStatus().equals("REJECTED")) {
            throw new AlreadyApprovedException("Loan application with id " + request.getLoanId() + " is already rejected");
        }

        approvalStatus.setStatus("REJECTED");
        approvalStatus.setReviewer(request.getApprover());
        approvalStatus.setMessage(request.getReason());
        approvalStatus.setApprovalDate(LocalDate.now());

        loanApplicationRepository.save(loanApplication);

        return new RejectLoanResponse("Loan application with id " + request.getLoanId() + " has been rejected", approvalStatus.getMessage());
    }

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

    public RepaymentResponse repayLoan(RepaymentRequest request) {
        LoanApplication loanApplication = loanApplicationRepository.findById(request.getLoanId())
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found with id: " + request.getLoanId()));

        ApprovalStatus approvalStatus = loanApplication.getApprovalStatus();
        if (!approvalStatus.getStatus().equals("APPROVED")) {
            throw new LoanNotApprovedException("Loan application with id " + request.getLoanId() + " is not approved");
        }

        BigDecimal loanAmount = loanApplication.getLoanAmount();
        BigDecimal interestRate = new BigDecimal("0.1"); // Interest rate (10%)
        BigDecimal totalAmountToBePaid = loanAmount.add(loanAmount.multiply(interestRate));

        BigDecimal totalRepaymentsMade = repaymentRepository.getTotalRepaymentsByLoanApplication(loanApplication);
        BigDecimal remainingBalance = totalAmountToBePaid.subtract(totalRepaymentsMade);

        BigDecimal repaymentAmount = request.getAmount();
        if (repaymentAmount.compareTo(remainingBalance) > 0) {
            throw new RepaymentAmountExceededException("Repayment amount exceeds remaining balance for loan application with id: " + request.getLoanId());
        }

        BigDecimal interest = remainingBalance.subtract(loanAmount);
        BigDecimal remainingAmountWithInterest = remainingBalance.subtract(repaymentAmount);

        Repayment repayment = new Repayment();
        repayment.setLoanApplication(loanApplication);
        repayment.setAmount(repaymentAmount);
        repayment.setPaymentDate(LocalDate.now());
        repayment.setInterest(interest);
        repayment.setPrincipal(repaymentAmount.subtract(interest));

        repaymentRepository.save(repayment);

        return new RepaymentResponse("Repayment recorded for loan application with id: " + request.getLoanId(), remainingAmountWithInterest);
    }

    private BigDecimal calculateInterest(LoanApplication loanApplication, BigDecimal amount) {
        BigDecimal interestRate = new BigDecimal("0.1"); // Setting the interest rate to (10%). In a real world application, I believe we should have another entity with rules of loan amount range and their respective interest rates.
        return amount.multiply(interestRate);
    }

    private BigDecimal calculateRemainingBalance(LoanApplication loanApplication) {
        BigDecimal totalLoanAmount = loanApplication.getLoanAmount();
        BigDecimal totalRepayments = repaymentRepository.getTotalRepaymentsByLoanApplication(loanApplication);
        return totalLoanAmount.subtract(totalRepayments);
    }
}
