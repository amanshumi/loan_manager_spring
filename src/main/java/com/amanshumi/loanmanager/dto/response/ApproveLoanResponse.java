package com.amanshumi.loanmanager.dto.response;

public class ApproveLoanResponse {
    private String status;
    private String reviewer;

    public ApproveLoanResponse(String status, String reviewer) {
        this.status = status;
        this.reviewer = reviewer;
    }

    public String getstatus() {
        return status;
    }

    public void setstatus(String status) {
        this.status = status;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }
}

