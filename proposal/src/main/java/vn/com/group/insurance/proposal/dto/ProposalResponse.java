package vn.com.group.insurance.proposal.dto;

import vn.com.group.insurance.proposal.entity.sqlserver.ProposalEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Proposal Response DTO.
 */
public class ProposalResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String proposalNumber;
    private String customerName;
    private String customerCode;
    private String planCode;
    private BigDecimal sumAssured;
    private BigDecimal premiumAmount;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;

    public ProposalResponse() {
    }

    public static ProposalResponse fromEntity(ProposalEntity entity) {
        if (entity == null) {
            return null;
        }
        ProposalResponse response = new ProposalResponse();
        response.setId(entity.getId());
        response.setProposalNumber(entity.getProposalNumber());
        response.setCustomerName(entity.getCustomerName());
        response.setCustomerCode(entity.getCustomerCode());
        response.setPlanCode(entity.getPlanCode());
        response.setSumAssured(entity.getSumAssured());
        response.setPremiumAmount(entity.getPremiumAmount());
        response.setStatus(entity.getStatus());
        response.setRemarks(entity.getRemarks());
        response.setCreatedAt(entity.getCreatedAt());
        response.setCreatedBy(entity.getCreatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
