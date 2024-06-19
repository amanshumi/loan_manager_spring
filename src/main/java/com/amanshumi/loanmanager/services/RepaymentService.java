package com.amanshumi.loanmanager.services;

import com.amanshumi.loanmanager.dto.request.RepaymentRequest;
import com.amanshumi.loanmanager.dto.response.RepaymentHistoryResponseDTO;
import com.amanshumi.loanmanager.dto.response.RepaymentResponse;
import com.amanshumi.loanmanager.entities.ApprovalStatus;
import com.amanshumi.loanmanager.entities.LoanApplication;
import com.amanshumi.loanmanager.entities.Repayment;
import com.amanshumi.loanmanager.exceptions.LoanApplicationNotFoundException;
import com.amanshumi.loanmanager.exceptions.LoanNotApprovedException;
import com.amanshumi.loanmanager.exceptions.RepaymentAmountExceededException;
import com.amanshumi.loanmanager.repositories.BorrowerRepository;
import com.amanshumi.loanmanager.repositories.LoanApplicationRepository;
import com.amanshumi.loanmanager.repositories.LoanDisbursementRepository;
import com.amanshumi.loanmanager.repositories.RepaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RepaymentService {
    @Autowired
    private LoanApplicationRepository loanApplicationRepository;
    @Autowired
    private LoanDisbursementRepository loanDisbursementRepository;
    @Autowired
    private RepaymentRepository repaymentRepository;
    @Autowired
    private BorrowerRepository borrowerRepository;

    private Logger myLoanLogger = LoggerFactory.getLogger(RepaymentService.class);

    public RepaymentResponse repayLoan(RepaymentRequest request) {
        myLoanLogger.info("Starting Loan Repayment Request : " + request.toString());

        LoanApplication loanApplication = loanApplicationRepository.findById(request.getLoanId())
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found with id: " + request.getLoanId()));

        ApprovalStatus approvalStatus = loanApplication.getApprovalStatus();
        if (!approvalStatus.getStatus().equals("APPROVED")) {
            myLoanLogger.error("Loan application with id : " + request.getLoanId() + " is not approved");
            throw new LoanNotApprovedException("Loan application with id " + request.getLoanId() + " is not approved");
        }

        BigDecimal loanAmount = loanApplication.getLoanAmount();
        BigDecimal interestRate = new BigDecimal("0.1"); // Interest rate (10%)
        BigDecimal totalAmountToBePaid = loanAmount.add(loanAmount.multiply(interestRate));
        BigDecimal totalInterest = loanAmount.multiply(interestRate);

        BigDecimal totalRepaymentsMade = repaymentRepository.getTotalRepaymentsByLoanApplication(loanApplication);
        BigDecimal remainingBalance = totalAmountToBePaid.subtract(totalRepaymentsMade);

        BigDecimal repaymentAmount = request.getAmount();
        if (repaymentAmount.compareTo(remainingBalance) > 0) {
            myLoanLogger.error("Repayment amount exceeds remaining balance for loan application : " + request.getLoanId());
            throw new RepaymentAmountExceededException("Repayment amount exceeds remaining balance for loan application with id: " + request.getLoanId());
        }

        BigDecimal interest = repaymentAmount.multiply(totalInterest).divide(totalAmountToBePaid, 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal principal = repaymentAmount.subtract(interest);
        BigDecimal remainingAmountWithInterest = remainingBalance.subtract(repaymentAmount);

        Repayment repayment = new Repayment();
        repayment.setLoanApplication(loanApplication);
        repayment.setAmount(repaymentAmount);
        repayment.setPaymentDate(LocalDate.now());
        repayment.setInterest(interest);
        repayment.setPrincipal(principal);

        repaymentRepository.save(repayment);

        myLoanLogger.info("Loan repayment completed for loan id : " + request.getLoanId() + ". Remaining amount is : " + remainingAmountWithInterest);

        return new RepaymentResponse("Repayment recorded for loan application with id: " + request.getLoanId(), remainingAmountWithInterest);
    }

    public RepaymentHistoryResponseDTO getRepaymentHistory(Long loanId) {
        myLoanLogger.info("Fetching loan repayment history for loan : " + loanId);
        LoanApplication loanApplication = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found with id: " + loanId));

        List<Repayment> repayments = repaymentRepository.findByLoanApplicationId(loanId);

        List<RepaymentHistoryResponseDTO.RepaymentDTO> repaymentDTOs = repayments.stream()
                .map(repayment -> {
                    RepaymentHistoryResponseDTO.RepaymentDTO repaymentDTO = new RepaymentHistoryResponseDTO.RepaymentDTO();
                    repaymentDTO.setAmount(repayment.getAmount());
                    repaymentDTO.setPaymentDate(repayment.getPaymentDate());
                    repaymentDTO.setInterest(repayment.getInterest());
                    repaymentDTO.setPrincipal(repayment.getPrincipal());
                    return repaymentDTO;
                })
                .collect(Collectors.toList());

        RepaymentHistoryResponseDTO responseDTO = new RepaymentHistoryResponseDTO();
        responseDTO.setLoanId(loanId);
        responseDTO.setRepayments(repaymentDTOs);

        myLoanLogger.info("Loan data fetched from db : " + responseDTO.toString());

        return responseDTO;
    }
}
