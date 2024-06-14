package com.amanshumi.loanmanager.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RepaymentHistoryResponseDTO {
    private Long loanId;
    private List<RepaymentDTO> repayments;

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public List<RepaymentDTO> getRepayments() {
        return repayments;
    }

    public void setRepayments(List<RepaymentDTO> repayments) {
        this.repayments = repayments;
    }

    public static class RepaymentDTO {
        private BigDecimal amount;
        private LocalDate paymentDate;
        private BigDecimal interest;
        private BigDecimal principal;

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public LocalDate getPaymentDate() {
            return paymentDate;
        }

        public void setPaymentDate(LocalDate paymentDate) {
            this.paymentDate = paymentDate;
        }

        public BigDecimal getInterest() {
            return interest;
        }

        public void setInterest(BigDecimal interest) {
            this.interest = interest;
        }

        public BigDecimal getPrincipal() {
            return principal;
        }

        public void setPrincipal(BigDecimal principal) {
            this.principal = principal;
        }
    }

}
