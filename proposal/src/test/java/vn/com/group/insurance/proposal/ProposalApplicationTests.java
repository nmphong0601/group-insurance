package vn.com.group.insurance.proposal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.com.group.insurance.proposal.dto.PolicyRecordDto;
import vn.com.group.insurance.proposal.dto.ProposalCreateRequest;
import vn.com.group.insurance.proposal.dto.ProposalResponse;
import vn.com.group.insurance.proposal.entity.db2.PolicyRecordEntity;
import vn.com.group.insurance.proposal.entity.sqlserver.ProposalEntity;
import vn.com.group.insurance.proposal.repository.db2.PolicyRecordRepository;
import vn.com.group.insurance.proposal.repository.sqlserver.ProposalRepository;
import vn.com.group.insurance.proposal.service.ProposalService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProposalApplicationTests {

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private PolicyRecordRepository policyRecordRepository;

    @Autowired
    private ProposalService proposalService;

    @Test
    @DisplayName("Context loads and both repositories are injected")
    void contextLoads() {
        assertThat(proposalRepository).isNotNull();
        assertThat(policyRecordRepository).isNotNull();
        assertThat(proposalService).isNotNull();
    }

    @Test
    @DisplayName("Test DbRepository Scannable, BatchReadable and BatchWritable methods on SQL Server repository")
    void testProposalDbRepositoryOperations() {
        ProposalEntity proposal = new ProposalEntity();
        proposal.setProposalNumber("PROP-TEST-001");
        proposal.setCustomerName("Nguyen Van A");
        proposal.setCustomerCode("CUST-1001");
        proposal.setPlanCode("LIFE-SHIELD-01");
        proposal.setSumAssured(new BigDecimal("500000000.00"));
        proposal.setPremiumAmount(new BigDecimal("25000000.00"));
        proposal.setStatus("PENDING");

        // BatchWritableRepository.save
        ProposalEntity saved = proposalRepository.save(proposal);
        assertThat(saved.getId()).isNotNull();

        // BatchReadableRepository.findById
        ProposalEntity found = Optional.of(proposalRepository.findOne(saved.getId())).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getCustomerName()).isEqualTo("Nguyen Van A");

        // ScannableRepository.findAll
        List<ProposalEntity> all = StreamSupport
                .stream(proposalRepository.findAll().spliterator(), false)
                .toList();
        assertThat(all).isNotEmpty();
    }

    @Test
    @DisplayName("Test DbRepository methods on IBM DB2 repository")
    void testPolicyRecordDbRepositoryOperations() {
        PolicyRecordEntity policy = new PolicyRecordEntity();
        policy.setPolicyNumber("POL-DB2-9999");
        policy.setCustomerCode("CUST-1001");
        policy.setProductType("HEALTH-CARE");
        policy.setIssueDate(LocalDate.of(2023, 1, 15));
        policy.setExpiryDate(LocalDate.of(2028, 1, 15));
        policy.setPolicyStatus("ACTIVE");
        policy.setTotalCoverage(new BigDecimal("300000000.00"));

        // BatchWritableRepository.save on DB2
        policyRecordRepository.save(policy);

        // BatchReadableRepository.findById on DB2
        PolicyRecordEntity found = Optional.of(policyRecordRepository.findOne("POL-DB2-9999")).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getProductType()).isEqualTo("HEALTH-CARE");
    }

    @Test
    @DisplayName("Test ProposalService multi-datasource linking between SQL Server and DB2")
    void testMultiDatasourceServiceIntegration() {
        // 1. Prepare legacy policy in DB2
        PolicyRecordEntity policy = new PolicyRecordEntity();
        policy.setPolicyNumber("POL-DB2-5555");
        policy.setCustomerCode("CUST-2002");
        policy.setProductType("TERM-LIFE");
        policy.setIssueDate(LocalDate.of(2022, 6, 1));
        policy.setExpiryDate(LocalDate.of(2032, 6, 1));
        policy.setPolicyStatus("ACTIVE");
        policy.setTotalCoverage(new BigDecimal("1000000000.00"));
        policyRecordRepository.save(policy);

        // 2. Create proposal in SQL Server via service
        ProposalCreateRequest request = new ProposalCreateRequest();
        request.setProposalNumber("PROP-SVC-2002");
        request.setCustomerName("Tran Thi B");
        request.setCustomerCode("CUST-2002");
        request.setPlanCode("TERM-LIFE-PLUS");
        request.setSumAssured(new BigDecimal("1000000000.00"));
        request.setPremiumAmount(new BigDecimal("15000000.00"));
        request.setRemarks("New online submission");

        ProposalResponse created = proposalService.createProposal(request);
        assertThat(created.getId()).isNotNull();

        // 3. Link proposal (SQL Server) with legacy policy (DB2)
        ProposalResponse linked = proposalService.linkProposalWithLegacyPolicy(created.getId(), "POL-DB2-5555");
        assertThat(linked.getRemarks()).contains("Linked to DB2 Legacy Policy [POL-DB2-5555]");

        // 4. Verify DB2 policy retrieval via service
        PolicyRecordDto db2Policy = proposalService.getLegacyPolicyByNumber("POL-DB2-5555");
        assertThat(db2Policy.getCustomerCode()).isEqualTo("CUST-2002");
    }
}
