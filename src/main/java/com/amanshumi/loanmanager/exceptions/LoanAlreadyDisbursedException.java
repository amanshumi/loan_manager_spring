package com.amanshumi.loanmanager.exceptions;

public class LoanAlreadyDisbursedException extends RuntimeException{

    public LoanAlreadyDisbursedException(String message) {
        super(message);
    }
}
