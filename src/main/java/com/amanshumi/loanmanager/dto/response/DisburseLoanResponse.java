package com.amanshumi.loanmanager.dto.response;

public class DisburseLoanResponse {
    private String message;

    public DisburseLoanResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

