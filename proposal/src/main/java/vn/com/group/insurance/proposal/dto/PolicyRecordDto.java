package vn.com.group.insurance.proposal.dto;

import vn.com.group.insurance.proposal.entity.db2.PolicyRecordEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Policy Record DTO representing data stored in IBM DB2.
 */
public class PolicyRecordDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String policyNumber;
    private String customerCode;
    private String productType;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String policyStatus;
    private BigDecimal totalCoverage;

    public PolicyRecordDto() {
    }

    public static PolicyRecordDto fromEntity(PolicyRecordEntity entity) {
        if (entity == null) {
            return null;
        }
        PolicyRecordDto dto = new PolicyRecordDto();
        dto.setPolicyNumber(entity.getPolicyNumber());
        dto.setCustomerCode(entity.getCustomerCode());
        dto.setProductType(entity.getProductType());
        dto.setIssueDate(entity.getIssueDate());
        dto.setExpiryDate(entity.getExpiryDate());
        dto.setPolicyStatus(entity.getPolicyStatus());
        dto.setTotalCoverage(entity.getTotalCoverage());
        return dto;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getPolicyStatus() {
        return policyStatus;
    }

    public void setPolicyStatus(String policyStatus) {
        this.policyStatus = policyStatus;
    }

    public BigDecimal getTotalCoverage() {
        return totalCoverage;
    }

    public void setTotalCoverage(BigDecimal totalCoverage) {
        this.totalCoverage = totalCoverage;
    }
}
