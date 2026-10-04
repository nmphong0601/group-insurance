package vn.com.group.insurance.proposal.repository.sqlserver;

import jp.xet.sparwings.spring.data.repository.DbRepository;
import org.springframework.stereotype.Repository;
import vn.com.group.insurance.proposal.entity.sqlserver.ProposalEntity;

import java.util.List;
import java.util.Optional;

/**
 * Proposal Repository backed by SQL Server datasource.
 * Extends {@link DbRepository} from jp.xet.sparwings.spring.data.repository.
 */
@Repository
public interface ProposalRepository extends DbRepository<ProposalEntity, Long> {

    Optional<ProposalEntity> findByProposalNumber(String proposalNumber);

    List<ProposalEntity> findByCustomerCode(String customerCode);

    List<ProposalEntity> findByStatus(String status);

    List<ProposalEntity> findByPlanCode(String planCode);
}
