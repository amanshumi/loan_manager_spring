package com.amanshumi.loanmanager.exceptions;

public class RepaymentAmountExceededException extends RuntimeException{
    public RepaymentAmountExceededException(String message) {
        super(message);
    }
}
