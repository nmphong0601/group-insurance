package vn.com.group.insurance.proposal.repository.db2;

import jp.xet.sparwings.spring.data.repository.DbRepository;
import org.springframework.stereotype.Repository;
import vn.com.group.insurance.proposal.entity.db2.PolicyRecordEntity;

import java.util.List;
import java.util.Optional;

/**
 * Policy Record Repository backed by IBM DB2 datasource.
 * Extends {@link DbRepository} from jp.xet.sparwings.spring.data.repository.
 */
@Repository
public interface PolicyRecordRepository extends DbRepository<PolicyRecordEntity, String> {

    Optional<PolicyRecordEntity> findByPolicyNumber(String policyNumber);

    List<PolicyRecordEntity> findByCustomerCode(String customerCode);

    List<PolicyRecordEntity> findByPolicyStatus(String policyStatus);
}
