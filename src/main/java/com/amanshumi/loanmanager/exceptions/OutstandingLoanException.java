package com.amanshumi.loanmanager.exceptions;

public class OutstandingLoanException extends RuntimeException {
    public OutstandingLoanException(String message) {
        super(message);
    }
}

