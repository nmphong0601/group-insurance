package vn.com.group.insurance.api.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for updating an existing insurance proposal.
 */
public class ProposalUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(max = 150, message = "Customer name cannot exceed 150 characters")
    private String customerName;

    @Size(max = 50, message = "Plan code cannot exceed 50 characters")
    private String planCode;

    @Positive(message = "Sum assured must be greater than zero")
    private BigDecimal sumAssured;

    @Positive(message = "Premium amount must be greater than zero")
    private BigDecimal premiumAmount;

    @Size(max = 20, message = "Status cannot exceed 20 characters")
    private String status;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    public ProposalUpdateRequest() {
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public BigDecimal getSumAssured() {
        return sumAssured;
    }

    public void setSumAssured(BigDecimal sumAssured) {
        this.sumAssured = sumAssured;
    }

    public BigDecimal getPremiumAmount() {
        return premiumAmount;
    }

    public void setPremiumAmount(BigDecimal premiumAmount) {
        this.premiumAmount = premiumAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
