package vn.com.group.insurance.api.service;

import vn.com.group.insurance.api.dto.PolicyRecordDto;
import vn.com.group.insurance.api.dto.ProposalCreateRequest;
import vn.com.group.insurance.api.dto.ProposalResponse;
import vn.com.group.insurance.api.dto.ProposalUpdateRequest;

import java.util.List;

/**
 * Service interface for Insurance Proposal operations across SQL Server and IBM DB2 datasources.
 */
public interface ProposalService {

    ProposalResponse createProposal(ProposalCreateRequest request);

    ProposalResponse getProposalById(Long id);

    ProposalResponse getProposalByNumber(String proposalNumber);

    List<ProposalResponse> getAllProposals();

    ProposalResponse updateProposal(Long id, ProposalUpdateRequest request);

    void deleteProposal(Long id);

    List<ProposalResponse> batchCreateProposals(List<ProposalCreateRequest> requests);

    // IBM DB2 legacy policy integration methods
    PolicyRecordDto getLegacyPolicyByNumber(String policyNumber);

    List<PolicyRecordDto> getLegacyPoliciesByCustomer(String customerCode);

    ProposalResponse linkProposalWithLegacyPolicy(Long proposalId, String policyNumber);
}
