package vn.com.group.insurance.proposal.entity.sqlserver;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import vn.com.group.insurance.common.entity.BaseEntity;

import java.math.BigDecimal;

/**
 * Proposal Entity managed by Microsoft SQL Server datasource.
 */
@Entity
@Table(name = "T_PROPOSAL")
public class ProposalEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROPOSAL_ID")
    private Long id;

    @Column(name = "PROPOSAL_NUMBER", nullable = false, unique = true, length = 30)
    private String proposalNumber;

    @Column(name = "CUSTOMER_NAME", nullable = false, length = 150)
    private String customerName;

    @Column(name = "CUSTOMER_CODE", nullable = false, length = 50)
    private String customerCode;

    @Column(name = "PLAN_CODE", nullable = false, length = 50)
    private String planCode;

    @Column(name = "SUM_ASSURED", precision = 18, scale = 2)
    private BigDecimal sumAssured;

    @Column(name = "PREMIUM_AMOUNT", precision = 18, scale = 2)
    private BigDecimal premiumAmount;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    public ProposalEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
