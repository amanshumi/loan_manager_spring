package com.amanshumi.loanmanager;

import com.amanshumi.loanmanager.dto.request.ApproveLoanRequest;
import com.amanshumi.loanmanager.dto.request.LoanApplicationRequestDTO;
import com.amanshumi.loanmanager.dto.request.RejectLoanRequest;
import com.amanshumi.loanmanager.dto.request.RepaymentRequest;
import com.amanshumi.loanmanager.dto.response.DisburseLoanResponse;
import com.amanshumi.loanmanager.dto.response.LoanApplicationResponseDTO;
import com.amanshumi.loanmanager.dto.response.LoanDetailsResponseDTO;
import com.amanshumi.loanmanager.dto.response.RepaymentHistoryResponseDTO;
import com.amanshumi.loanmanager.dto.response.RepaymentResponse;
import com.amanshumi.loanmanager.entities.ApprovalStatus;
import com.amanshumi.loanmanager.entities.Borrower;
import com.amanshumi.loanmanager.entities.LoanApplication;
import com.amanshumi.loanmanager.entities.LoanDisbursement;
import com.amanshumi.loanmanager.entities.Repayment;
import com.amanshumi.loanmanager.exceptions.LoanAlreadyDisbursedException;
import com.amanshumi.loanmanager.exceptions.LoanApplicationNotFoundException;
import com.amanshumi.loanmanager.exceptions.LoanNotApprovedException;
import com.amanshumi.loanmanager.exceptions.RepaymentAmountExceededException;
import com.amanshumi.loanmanager.repositories.BorrowerRepository;
import com.amanshumi.loanmanager.repositories.LoanApplicationRepository;
import com.amanshumi.loanmanager.repositories.LoanDisbursementRepository;
import com.amanshumi.loanmanager.repositories.RepaymentRepository;
import com.amanshumi.loanmanager.services.DisbursementService;
import com.amanshumi.loanmanager.services.LoanApplicationService;
import com.amanshumi.loanmanager.services.RepaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTests {

    @Mock
    private LoanApplicationRepository loanAppRepository;

    @Mock
    private LoanDisbursementRepository loanDisbursementRepository;

    @Mock
    private RepaymentRepository repaymentRepository;

    @InjectMocks
    private DisbursementService disbursementService;

    @InjectMocks
    private RepaymentService repaymentService;

    @Mock
    private BorrowerRepository borrowerRepo;

    @InjectMocks
    private LoanApplicationService loanService;

    private LoanApplicationRequestDTO loanRequest;
    private Borrower borrowerDetails;
    private LoanApplication loanApp;

    private LoanApplication loanAppDisburse;

    @BeforeEach
    void setupTestData() {
        loanRequest = new LoanApplicationRequestDTO();
        loanRequest.setBorrowerName("Amanuel Shumi");
        loanRequest.setBorrowerAddress("Kolfe Keranio, Addis Ababa");
        loanRequest.setBorrowerPhoneNumber("0979611114");
        loanRequest.setBorrowerEmail("amanuelshumi14@gmail.com");
        loanRequest.setBorrowerIncome(new BigDecimal("60000"));
        loanRequest.setBorrowerCreditScore(650);
        loanRequest.setLoanAmount(new BigDecimal("15000"));
        loanRequest.setTerm(24);
        loanRequest.setPurpose("Sales and Promotion");

        borrowerDetails = new Borrower();
        borrowerDetails.setId(2L);
        borrowerDetails.setName("Amanuel Shumi");
        borrowerDetails.setEmail("amanuelshumi14@gmail.com");

        loanApp = new LoanApplication();
        loanApp.setId(2L);
        loanApp.setBorrower(borrowerDetails);
        loanApp.setLoanAmount(new BigDecimal("15000"));
        loanApp.setTerm(24);
        loanApp.setPurpose("Sales and Promotion");
        loanApp.setApplicationDate(LocalDate.now());
        ApprovalStatus approvalStatus = new ApprovalStatus();
        approvalStatus.setStatus("PENDING");
        loanApp.setApprovalStatus(approvalStatus);

        loanAppDisburse = new LoanApplication();
        loanAppDisburse.setId(1L);
        loanAppDisburse.setLoanAmount(new BigDecimal("10000"));
        ApprovalStatus approvalStatus2 = new ApprovalStatus();
        approvalStatus2.setStatus("APPROVED");
        loanAppDisburse.setApprovalStatus(approvalStatus2);
    }

    @Test
    void testSubmitNewLoanApplication() {
        when(borrowerRepo.findByEmail(loanRequest.getBorrowerEmail())).thenReturn(Optional.empty());
        when(borrowerRepo.save(any(Borrower.class))).thenReturn(borrowerDetails);
        when(loanAppRepository.save(any(LoanApplication.class))).thenReturn(loanApp);

        LoanApplicationResponseDTO response = loanService.submitLoanApplication(loanRequest);

        assertNotNull(response);
        assertEquals(loanApp.getId(), response.getId());
        assertEquals(borrowerDetails.getId(), response.getBorrowerId());
        assertEquals(loanApp.getLoanAmount(), response.getLoanAmount());
        assertEquals(loanApp.getTerm(), response.getTerm());
        assertEquals(loanApp.getPurpose(), response.getPurpose());
        assertEquals("PENDING", response.getStatus());
        assertEquals(loanApp.getApplicationDate(), response.getApplicationDate());
    }

    @Test
    void testGetLoanDetails_ValidLoanId() {
        when(loanAppRepository.findByIdWithBorrower(loanApp.getId())).thenReturn(loanApp);

        LoanDetailsResponseDTO response = loanService.getLoanDetailsById(loanApp.getId());

        assertNotNull(response);
        assertEquals(loanApp.getId(), response.getId());
        assertEquals(borrowerDetails.getId(), response.getBorrower().getId());
        assertEquals(borrowerDetails.getName(), response.getBorrower().getName());
        assertEquals(borrowerDetails.getEmail(), response.getBorrower().getEmail());
        assertEquals(borrowerDetails.getPhoneNumber(), response.getBorrower().getPhoneNumber());
        assertEquals(loanApp.getLoanAmount(), response.getLoanAmount());
        assertEquals(loanApp.getTerm(), response.getTerm());
        assertEquals(loanApp.getPurpose(), response.getPurpose());
        assertEquals(loanApp.getApprovalStatus().getStatus(), response.getStatus());
        assertEquals(loanApp.getApplicationDate(), response.getApplicationDate());
    }

    @Test
    void testGetLoanDetails_InvalidLoanId() {
        when(loanAppRepository.findByIdWithBorrower(loanApp.getId())).thenReturn(null);

        assertThrows(LoanApplicationNotFoundException.class, () -> loanService.getLoanDetailsById(loanApp.getId()));
    }

    @Test
    void testApproveLoanApplication() {
        when(loanAppRepository.findById(loanApp.getId())).thenReturn(Optional.of(loanApp));

        ApproveLoanRequest request = new ApproveLoanRequest();
        request.setLoanId(loanApp.getId());
        request.setApprover("Abebe Mikael");

        loanService.approveLoanApplication(request);

        assertEquals("APPROVED", loanApp.getApprovalStatus().getStatus());
        assertEquals("Abebe Mikael", loanApp.getApprovalStatus().getReviewer());
        assertNotNull(loanApp.getApprovalStatus().getApprovalDate());
    }

    @Test
    void testRejectLoanApplication() {
        when(loanAppRepository.findById(loanApp.getId())).thenReturn(Optional.of(loanApp));

        RejectLoanRequest request = new RejectLoanRequest();
        request.setLoanId(loanApp.getId());
        request.setApprover("Tigist Melese");
        request.setReason("Low credit score");

        loanService.rejectLoan(request);

        assertEquals("REJECTED", loanApp.getApprovalStatus().getStatus());
        assertEquals("Tigist Melese", loanApp.getApprovalStatus().getReviewer());
        assertEquals("Low credit score", loanApp.getApprovalStatus().getMessage());
        assertNotNull(loanApp.getApprovalStatus().getApprovalDate());
    }

    // Disbursement tests
    @Test
    void testDisburseLoan_LoanNotApproved() {
        loanApp.getApprovalStatus().setStatus("PENDING");
        when(loanAppRepository.findById(loanApp.getId())).thenReturn(Optional.of(loanApp));

        assertThrows(LoanNotApprovedException.class, () -> disbursementService.disburseLoan(loanApp.getId()));
    }

    @Test
    void testDisburseLoan_LoanAlreadyDisbursedException() {
        loanApp.getApprovalStatus().setStatus("APPROVED");

        when(loanAppRepository.findById(loanApp.getId())).thenReturn(Optional.of(loanApp));
        when(loanDisbursementRepository.findByLoanApplication(loanApp)).thenReturn(new LoanDisbursement());

        LoanAlreadyDisbursedException exception = assertThrows(LoanAlreadyDisbursedException.class, () -> {
            disbursementService.disburseLoan(loanApp.getId());
        });

        assertEquals("Loan application with id " + loanApp.getId() + " is already disbursed", exception.getMessage());
    }

    @Test
    void testDisburseLoan_ValidApprovedLoan() {
        LoanApplication loanApp = new LoanApplication();
        loanApp.setId(1L);
        loanApp.setLoanAmount(new BigDecimal("10000"));
        ApprovalStatus approvalStatus = new ApprovalStatus();
        approvalStatus.setStatus("APPROVED");
        loanApp.setApprovalStatus(approvalStatus);

        when(loanAppRepository.findById(loanApp.getId())).thenReturn(Optional.of(loanApp));
        when(loanDisbursementRepository.findByLoanApplication(loanApp)).thenReturn(null);
        when(loanDisbursementRepository.save(any(LoanDisbursement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DisburseLoanResponse response = disbursementService.disburseLoan(loanApp.getId());

        assertNotNull(response);
        assertTrue(response.getMessage().contains("Loan application with id " + loanApp.getId() + " has been disbursed"));
        verify(loanDisbursementRepository, times(1)).save(any(LoanDisbursement.class));
    }

    @Test
    void testDisburseLoan_LoanApplicationNotFoundException() {
        when(loanAppRepository.findById(loanApp.getId())).thenReturn(Optional.empty());

        LoanApplicationNotFoundException exception = assertThrows(LoanApplicationNotFoundException.class, () -> {
            disbursementService.disburseLoan(loanApp.getId());
        });

        assertEquals("Loan application not found with id: " + loanApp.getId(), exception.getMessage());
    }

    // Repayment tests
    @Test
    void testRepayLoan() {
        RepaymentRequest repaymentRequest = new RepaymentRequest();
        repaymentRequest.setLoanId(1L);
        repaymentRequest.setAmount(new BigDecimal("1000"));

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(1L);
        loanApplication.setLoanAmount(new BigDecimal("5000"));
        ApprovalStatus approvalStatus = new ApprovalStatus();
        approvalStatus.setStatus("APPROVED");
        loanApplication.setApprovalStatus(approvalStatus);

        when(loanAppRepository.findById(1L)).thenReturn(Optional.of(loanApplication));
        when(repaymentRepository.getTotalRepaymentsByLoanApplication(loanApplication)).thenReturn(new BigDecimal("2000"));
        when(repaymentRepository.save(any(Repayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RepaymentResponse response = repaymentService.repayLoan(repaymentRequest);

        assertNotNull(response);
        assertEquals("Repayment recorded for loan application with id: 1", response.getMessage());
        verify(repaymentRepository, times(1)).save(any(Repayment.class));
    }

    @Test
    void testRepayLoan_AmountExceeded() {
        RepaymentRequest repaymentRequest = new RepaymentRequest();
        repaymentRequest.setLoanId(1L);
        repaymentRequest.setAmount(new BigDecimal("4000"));

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(1L);
        loanApplication.setLoanAmount(new BigDecimal("5000"));
        ApprovalStatus approvalStatus = new ApprovalStatus();
        approvalStatus.setStatus("APPROVED");
        loanApplication.setApprovalStatus(approvalStatus);

        when(loanAppRepository.findById(1L)).thenReturn(Optional.of(loanApplication));
        when(repaymentRepository.getTotalRepaymentsByLoanApplication(loanApplication)).thenReturn(new BigDecimal("2000"));

        assertThrows(RepaymentAmountExceededException.class, () -> repaymentService.repayLoan(repaymentRequest));
    }

    @Test
    void testGetRepaymentHistory() {
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(1L);

        Repayment repayment1 = new Repayment();
        repayment1.setId(1L);
        repayment1.setAmount(new BigDecimal("1000"));
        repayment1.setPaymentDate(LocalDate.now());
        repayment1.setInterest(new BigDecimal("100"));
        repayment1.setPrincipal(new BigDecimal("900"));

        Repayment repayment2 = new Repayment();
        repayment2.setId(2L);
        repayment2.setAmount(new BigDecimal("2000"));
        repayment2.setPaymentDate(LocalDate.now().minusDays(1));
        repayment2.setInterest(new BigDecimal("200"));
        repayment2.setPrincipal(new BigDecimal("1800"));

        List<Repayment> repayments = Arrays.asList(repayment1, repayment2);

        when(loanAppRepository.findById(1L)).thenReturn(Optional.of(loanApplication));
        when(repaymentRepository.findByLoanApplicationId(1L)).thenReturn(repayments);

        RepaymentHistoryResponseDTO response = repaymentService.getRepaymentHistory(1L);

        assertNotNull(response);
        assertEquals(2, response.getRepayments().size());
        assertEquals(new BigDecimal("1000"), response.getRepayments().get(0).getAmount());
        assertEquals(new BigDecimal("2000"), response.getRepayments().get(1).getAmount());
        verify(repaymentRepository, times(1)).findByLoanApplicationId(1L);
    }
}
