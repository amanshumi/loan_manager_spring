package com.amanshumi.loanmanager.dto.response;

import java.math.BigDecimal;

public class RepaymentResponse {
    private String message;

    private BigDecimal remainingAmount;
    public RepaymentResponse(String message, BigDecimal remainingAmount) {
        this.message = message;
        this.remainingAmount = remainingAmount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
}
