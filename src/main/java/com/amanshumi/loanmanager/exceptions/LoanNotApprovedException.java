package com.amanshumi.loanmanager.exceptions;

public class LoanNotApprovedException extends RuntimeException{

    public LoanNotApprovedException(String message) {
        super(message);
    }
}
