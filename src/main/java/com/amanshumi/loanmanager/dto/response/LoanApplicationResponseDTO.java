package com.amanshumi.loanmanager.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanApplicationResponseDTO {
    private Long id;
    private Long borrowerId;
    private BigDecimal loanAmount;
    private int term;
    private String purpose;
    private String status;
    private LocalDate applicationDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    @Override
    public String toString() {
        return "LoanApplicationResponseDTO{" +
                "id=" + id +
                ", borrowerId=" + borrowerId +
                ", loanAmount=" + loanAmount +
                ", term=" + term +
                ", purpose='" + purpose + '\'' +
                ", status='" + status + '\'' +
                ", applicationDate=" + applicationDate +
                '}';
    }
}

