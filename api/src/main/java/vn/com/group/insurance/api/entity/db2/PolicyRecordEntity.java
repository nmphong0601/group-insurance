package vn.com.group.insurance.api.entity.db2;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import vn.com.group.insurance.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Legacy Policy Record Entity managed by IBM DB2 datasource.
 */
@Entity
@Table(name = "T_LEGACY_POLICY")
public class PolicyRecordEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "POLICY_NUMBER", length = 30, nullable = false)
    private String policyNumber;

    @Column(name = "CUSTOMER_CODE", length = 50, nullable = false)
    private String customerCode;

    @Column(name = "PRODUCT_TYPE", length = 50, nullable = false)
    private String productType;

    @Column(name = "ISSUE_DATE")
    private LocalDate issueDate;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

    @Column(name = "POLICY_STATUS", length = 20, nullable = false)
    private String policyStatus;

    @Column(name = "TOTAL_COVERAGE", precision = 18, scale = 2)
    private BigDecimal totalCoverage;

    public PolicyRecordEntity() {
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
