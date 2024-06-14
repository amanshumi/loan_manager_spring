package com.amanshumi.loanmanager.dto.request;

import java.math.BigDecimal;

public class LoanApplicationRequestDTO {
    private String borrowerName;
    private String borrowerAddress;
    private String borrowerPhoneNumber;
    private String borrowerEmail;
    private BigDecimal borrowerIncome;
    private int borrowerCreditScore;
    private BigDecimal loanAmount;
    private int term; // in months
    private String purpose;

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public String getBorrowerAddress() {
        return borrowerAddress;
    }

    public void setBorrowerAddress(String borrowerAddress) {
        this.borrowerAddress = borrowerAddress;
    }

    public String getBorrowerPhoneNumber() {
        return borrowerPhoneNumber;
    }

    public void setBorrowerPhoneNumber(String borrowerPhoneNumber) {
        this.borrowerPhoneNumber = borrowerPhoneNumber;
    }

    public String getBorrowerEmail() {
        return borrowerEmail;
    }

    public void setBorrowerEmail(String borrowerEmail) {
        this.borrowerEmail = borrowerEmail;
    }

    public BigDecimal getBorrowerIncome() {
        return borrowerIncome;
    }

    public void setBorrowerIncome(BigDecimal borrowerIncome) {
        this.borrowerIncome = borrowerIncome;
    }

    public int getBorrowerCreditScore() {
        return borrowerCreditScore;
    }

    public void setBorrowerCreditScore(int borrowerCreditScore) {
        this.borrowerCreditScore = borrowerCreditScore;
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
}
