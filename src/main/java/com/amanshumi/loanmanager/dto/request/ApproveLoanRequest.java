package com.amanshumi.loanmanager.dto.request;

public class ApproveLoanRequest {
    private Long loanId;
    private String approver;

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }
}
