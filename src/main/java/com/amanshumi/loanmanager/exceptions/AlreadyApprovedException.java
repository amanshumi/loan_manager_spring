package com.amanshumi.loanmanager.exceptions;

public class AlreadyApprovedException extends RuntimeException{
    public AlreadyApprovedException(String message) {
        super(message);
    }
}
