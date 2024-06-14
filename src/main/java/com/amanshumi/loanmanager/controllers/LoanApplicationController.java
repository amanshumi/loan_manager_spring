package com.amanshumi.loanmanager.controllers;

import com.amanshumi.loanmanager.dto.request.ApproveLoanRequest;
import com.amanshumi.loanmanager.dto.request.LoanApplicationRequestDTO;
import com.amanshumi.loanmanager.dto.request.RejectLoanRequest;
import com.amanshumi.loanmanager.dto.request.RepaymentRequest;
import com.amanshumi.loanmanager.dto.response.*;
import com.amanshumi.loanmanager.services.LoanApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
public class LoanApplicationController {

    @Autowired
    private LoanApplicationService loanApplicationService;

    @PostMapping("/apply")
    public ResponseEntity<LoanApplicationResponseDTO> submitLoanApplication(@RequestBody LoanApplicationRequestDTO request) {
        LoanApplicationResponseDTO response = loanApplicationService.submitLoanApplication(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/details/{loanId}")
    public ResponseEntity<LoanDetailsResponseDTO> getLoanDetailsById(@PathVariable Long loanId) {
        LoanDetailsResponseDTO loanResponseDTO = loanApplicationService.getLoanDetailsById(loanId);
        return new ResponseEntity<>(loanResponseDTO, HttpStatus.OK);
    }

    @PutMapping("/approve")
    public ResponseEntity<ApproveLoanResponse> approveLoanApplication(@RequestBody ApproveLoanRequest request) {
        ApproveLoanResponse response = loanApplicationService.approveLoanApplication(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/reject")
    public ResponseEntity<RejectLoanResponse> rejectLoanApplication(@RequestBody RejectLoanRequest request) {
        RejectLoanResponse response = loanApplicationService.rejectLoan(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{loanId}/disburse")
    public ResponseEntity<DisburseLoanResponse> disburseLoan(@PathVariable Long loanId) {
        DisburseLoanResponse response = loanApplicationService.disburseLoan(loanId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{loanId}/repay")
    public ResponseEntity<RepaymentResponse> repayLoan(@RequestBody RepaymentRequest request) {
        RepaymentResponse response = loanApplicationService.repayLoan(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{loanId}/repayment-history")
    public ResponseEntity<RepaymentHistoryResponseDTO> getRepaymentHistory(@PathVariable Long loanId) {
        RepaymentHistoryResponseDTO repaymentHistory = loanApplicationService.getRepaymentHistory(loanId);
        return ResponseEntity.ok(repaymentHistory);
    }
}
