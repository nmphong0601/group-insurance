package vn.com.group.insurance.proposal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for creating a new insurance proposal.
 */
public class ProposalCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Proposal number is required")
    @Size(max = 30, message = "Proposal number cannot exceed 30 characters")
    private String proposalNumber;

    @NotBlank(message = "Customer name is required")
    @Size(max = 150, message = "Customer name cannot exceed 150 characters")
    private String customerName;

    @NotBlank(message = "Customer code is required")
    @Size(max = 50, message = "Customer code cannot exceed 50 characters")
    private String customerCode;

    @NotBlank(message = "Plan code is required")
    @Size(max = 50, message = "Plan code cannot exceed 50 characters")
    private String planCode;

    @NotNull(message = "Sum assured is required")
    @Positive(message = "Sum assured must be greater than zero")
    private BigDecimal sumAssured;

    @NotNull(message = "Premium amount is required")
    @Positive(message = "Premium amount must be greater than zero")
    private BigDecimal premiumAmount;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    public ProposalCreateRequest() {
    }

    public String getProposalNumber() {
        return proposalNumber;
    }

    public void setProposalNumber(String proposalNumber) {
        this.proposalNumber = proposalNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
