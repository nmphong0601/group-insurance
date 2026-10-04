package vn.com.group.insurance.proposal.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.group.insurance.common.dto.ApiResponse;
import vn.com.group.insurance.proposal.dto.BatchProposalRequest;
import vn.com.group.insurance.proposal.dto.PolicyRecordDto;
import vn.com.group.insurance.proposal.dto.ProposalCreateRequest;
import vn.com.group.insurance.proposal.dto.ProposalResponse;
import vn.com.group.insurance.proposal.dto.ProposalUpdateRequest;
import vn.com.group.insurance.proposal.service.ProposalService;

import java.util.List;

/**
 * REST Controller exposing Proposal microservice endpoints.
 */
@RestController
@RequestMapping("/api/v1/proposals")
public class ProposalController {

    private final ProposalService proposalService;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProposalResponse>> createProposal(@Valid @RequestBody ProposalCreateRequest request) {
        ProposalResponse response = proposalService.createProposal(request);
        return new ResponseEntity<>(ApiResponse.created("Proposal created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProposalResponse>>> getAllProposals() {
        List<ProposalResponse> proposals = proposalService.getAllProposals();
        return ResponseEntity.ok(ApiResponse.ok("Retrieved all proposals", proposals));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProposalResponse>> getProposalById(@PathVariable Long id) {
        ProposalResponse response = proposalService.getProposalById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/number/{proposalNumber}")
    public ResponseEntity<ApiResponse<ProposalResponse>> getProposalByNumber(@PathVariable String proposalNumber) {
        ProposalResponse response = proposalService.getProposalByNumber(proposalNumber);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProposalResponse>> updateProposal(
            @PathVariable Long id,
            @Valid @RequestBody ProposalUpdateRequest request) {
        ProposalResponse response = proposalService.updateProposal(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Proposal updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProposal(@PathVariable Long id) {
        proposalService.deleteProposal(id);
        return ResponseEntity.ok(ApiResponse.ok("Proposal deleted successfully", null));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<ProposalResponse>>> batchCreateProposals(
            @Valid @RequestBody BatchProposalRequest batchRequest) {
        List<ProposalResponse> responses = proposalService.batchCreateProposals(batchRequest.getProposals());
        return new ResponseEntity<>(ApiResponse.created("Batch proposals created successfully", responses), HttpStatus.CREATED);
    }

    // =========================================================================
    // IBM DB2 Endpoints
    // =========================================================================

    @GetMapping("/db2/policies/{policyNumber}")
    public ResponseEntity<ApiResponse<PolicyRecordDto>> getLegacyPolicy(@PathVariable String policyNumber) {
        PolicyRecordDto policy = proposalService.getLegacyPolicyByNumber(policyNumber);
        return ResponseEntity.ok(ApiResponse.ok("Retrieved legacy policy from IBM DB2", policy));
    }

    @GetMapping("/db2/customers/{customerCode}/policies")
    public ResponseEntity<ApiResponse<List<PolicyRecordDto>>> getLegacyPoliciesByCustomer(@PathVariable String customerCode) {
        List<PolicyRecordDto> policies = proposalService.getLegacyPoliciesByCustomer(customerCode);
        return ResponseEntity.ok(ApiResponse.ok("Retrieved legacy policies from IBM DB2", policies));
    }

    @PostMapping("/{id}/link-db2-policy/{policyNumber}")
    public ResponseEntity<ApiResponse<ProposalResponse>> linkProposalWithLegacyPolicy(
            @PathVariable Long id,
            @PathVariable String policyNumber) {
        ProposalResponse response = proposalService.linkProposalWithLegacyPolicy(id, policyNumber);
        return ResponseEntity.ok(ApiResponse.ok("Proposal linked with DB2 legacy policy", response));
    }
}
