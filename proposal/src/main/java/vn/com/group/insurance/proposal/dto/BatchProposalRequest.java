package vn.com.group.insurance.proposal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;

/**
 * Request payload for batch saving proposals.
 */
public class BatchProposalRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Proposals list must not be empty")
    @Valid
    private List<ProposalCreateRequest> proposals;

    public BatchProposalRequest() {
    }

    public List<ProposalCreateRequest> getProposals() {
        return proposals;
    }

    public void setProposals(List<ProposalCreateRequest> proposals) {
        this.proposals = proposals;
    }
}
