package vn.com.group.insurance.api.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.group.insurance.common.constant.CommonConstants;
import vn.com.group.insurance.common.exception.BadRequestException;
import vn.com.group.insurance.common.exception.ResourceNotFoundException;
import vn.com.group.insurance.api.dto.PolicyRecordDto;
import vn.com.group.insurance.api.dto.ProposalCreateRequest;
import vn.com.group.insurance.api.dto.ProposalResponse;
import vn.com.group.insurance.api.dto.ProposalUpdateRequest;
import vn.com.group.insurance.api.entity.db2.PolicyRecordEntity;
import vn.com.group.insurance.api.entity.sqlserver.ProposalEntity;
import vn.com.group.insurance.api.repository.db2.PolicyRecordRepository;
import vn.com.group.insurance.api.repository.sqlserver.ProposalRepository;
import vn.com.group.insurance.api.service.ProposalService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service implementation managing business logic across SQL Server (Proposal) and DB2 (Legacy Policy) databases.
 */
@Service
public class ProposalServiceImpl implements ProposalService {

    private static final Logger log = LoggerFactory.getLogger(ProposalServiceImpl.class);

    private final ProposalRepository proposalRepository;
    private final PolicyRecordRepository policyRecordRepository;

    public ProposalServiceImpl(ProposalRepository proposalRepository, PolicyRecordRepository policyRecordRepository) {
        this.proposalRepository = proposalRepository;
        this.policyRecordRepository = policyRecordRepository;
    }

    @Override
    @Transactional(transactionManager = "sqlServerTransactionManager")
    public ProposalResponse createProposal(ProposalCreateRequest request) {
        log.info("Creating proposal with proposalNumber: {}", request.getProposalNumber());

        if (proposalRepository.findByProposalNumber(request.getProposalNumber()).isPresent()) {
            throw new BadRequestException("PROPOSAL_ALREADY_EXISTS",
                    "Proposal with number [" + request.getProposalNumber() + "] already exists.");
        }

        ProposalEntity entity = new ProposalEntity();
        entity.setProposalNumber(request.getProposalNumber());
        entity.setCustomerName(request.getCustomerName());
        entity.setCustomerCode(request.getCustomerCode());
        entity.setPlanCode(request.getPlanCode());
        entity.setSumAssured(request.getSumAssured());
        entity.setPremiumAmount(request.getPremiumAmount());
        entity.setStatus(CommonConstants.STATUS_PENDING);
        entity.setRemarks(request.getRemarks());

        // Uses BatchWritableRepository.save(entity) via DbRepository
        ProposalEntity saved = proposalRepository.save(entity);
        log.info("Successfully created proposal with ID: {}", saved.getId());
        return ProposalResponse.fromEntity(saved);
    }

    @Override
    @Transactional(transactionManager = "sqlServerTransactionManager", readOnly = true)
    public ProposalResponse getProposalById(Long id) {
        log.debug("Fetching proposal by ID: {}", id);
        // Uses BatchReadableRepository.findById(id) via DbRepository
        ProposalEntity entity = Optional.of(proposalRepository.findOne(id))
                .orElseThrow(() -> new ResourceNotFoundException("Proposal", id));
        return ProposalResponse.fromEntity(entity);
    }

    @Override
    @Transactional(transactionManager = "sqlServerTransactionManager", readOnly = true)
    public ProposalResponse getProposalByNumber(String proposalNumber) {
        log.debug("Fetching proposal by proposalNumber: {}", proposalNumber);
        ProposalEntity entity = proposalRepository.findByProposalNumber(proposalNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal", proposalNumber));
        return ProposalResponse.fromEntity(entity);
    }

    @Override
    @Transactional(transactionManager = "sqlServerTransactionManager", readOnly = true)
    public List<ProposalResponse> getAllProposals() {
        log.debug("Scanning all proposals from SQL Server");
        // Uses ScannableRepository.findAll() via DbRepository
        List<ProposalEntity> list = StreamSupport
                .stream(proposalRepository.findAll().spliterator(), false)
                .toList();
        return list.stream()
                .map(ProposalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(transactionManager = "sqlServerTransactionManager")
    public ProposalResponse updateProposal(Long id, ProposalUpdateRequest request) {
        log.info("Updating proposal with ID: {}", id);
        ProposalEntity entity = Optional.of(proposalRepository.findOne(id))
                .orElseThrow(() -> new ResourceNotFoundException("Proposal", id));

        if (request.getCustomerName() != null) {
            entity.setCustomerName(request.getCustomerName());
        }
        if (request.getPlanCode() != null) {
            entity.setPlanCode(request.getPlanCode());
        }
        if (request.getSumAssured() != null) {
            entity.setSumAssured(request.getSumAssured());
        }
        if (request.getPremiumAmount() != null) {
            entity.setPremiumAmount(request.getPremiumAmount());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getRemarks() != null) {
            entity.setRemarks(request.getRemarks());
        }

        // Uses BatchWritableRepository.save(entity) via DbRepository
        ProposalEntity updated = proposalRepository.save(entity);
        return ProposalResponse.fromEntity(updated);
    }

    @Override
    @Transactional(transactionManager = "sqlServerTransactionManager")
    public void deleteProposal(Long id) {
        log.info("Deleting proposal with ID: {}", id);
        if (!proposalRepository.exists(id)) {
            throw new ResourceNotFoundException("Proposal", id);
        }
        // Uses BatchWritableRepository.deleteById(id) via DbRepository
        proposalRepository.delete(id);
    }

    @Override
    @Transactional(transactionManager = "sqlServerTransactionManager")
    public List<ProposalResponse> batchCreateProposals(List<ProposalCreateRequest> requests) {
        log.info("Batch creating {} proposals", requests.size());
        List<ProposalEntity> entities = new ArrayList<>();
        for (ProposalCreateRequest req : requests) {
            ProposalEntity entity = new ProposalEntity();
            entity.setProposalNumber(req.getProposalNumber());
            entity.setCustomerName(req.getCustomerName());
            entity.setCustomerCode(req.getCustomerCode());
            entity.setPlanCode(req.getPlanCode());
            entity.setSumAssured(req.getSumAssured());
            entity.setPremiumAmount(req.getPremiumAmount());
            entity.setStatus(CommonConstants.STATUS_PENDING);
            entity.setRemarks(req.getRemarks());
            entities.add(entity);
        }

        // Uses BatchWritableRepository.saveAll(entities) via DbRepository
        // List<ProposalEntity> savedEntities = proposalRepository.save(entities);
        proposalRepository.save(entities);
        List<ProposalEntity> savedEntities = entities.stream().toList();
        return savedEntities.stream()
                .map(ProposalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // IBM DB2 Integration Operations
    // =========================================================================

    @Override
    @Transactional(transactionManager = "db2TransactionManager", readOnly = true)
    public PolicyRecordDto getLegacyPolicyByNumber(String policyNumber) {
        log.debug("Querying IBM DB2 for policyNumber: {}", policyNumber);
        // Uses BatchReadableRepository.findById(id) via DbRepository
        PolicyRecordEntity policy = Optional.of(policyRecordRepository.findOne(policyNumber))
                .orElseThrow(() -> new ResourceNotFoundException("Legacy Policy", policyNumber));
        return PolicyRecordDto.fromEntity(policy);
    }

    @Override
    @Transactional(transactionManager = "db2TransactionManager", readOnly = true)
    public List<PolicyRecordDto> getLegacyPoliciesByCustomer(String customerCode) {
        log.debug("Querying IBM DB2 for customerCode: {}", customerCode);
        return policyRecordRepository.findByCustomerCode(customerCode).stream()
                .map(PolicyRecordDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(transactionManager = "sqlServerTransactionManager")
    public ProposalResponse linkProposalWithLegacyPolicy(Long proposalId, String policyNumber) {
        log.info("Linking proposal ID {} with DB2 legacy policy {}", proposalId, policyNumber);

        // 1. Fetch proposal from SQL Server
        ProposalEntity proposal = Optional.of(proposalRepository.findOne(proposalId))
                .orElseThrow(() -> new ResourceNotFoundException("Proposal", proposalId));

        // 2. Query legacy policy from IBM DB2
        PolicyRecordEntity policy = Optional.of(policyRecordRepository.findOne(policyNumber))
                .orElseThrow(() -> new ResourceNotFoundException("Legacy Policy", policyNumber));

        // 3. Validate customer code match across SQL Server and DB2
        if (!proposal.getCustomerCode().equalsIgnoreCase(policy.getCustomerCode())) {
            throw new BadRequestException("CUSTOMER_MISMATCH",
                    String.format("Proposal customer [%s] does not match policy customer [%s]",
                            proposal.getCustomerCode(), policy.getCustomerCode()));
        }

        // 4. Update proposal with reference remarks and save back to SQL Server
        proposal.setRemarks("Linked to DB2 Legacy Policy [" + policyNumber + "] Status: " + policy.getPolicyStatus());
        ProposalEntity updated = proposalRepository.save(proposal);

        return ProposalResponse.fromEntity(updated);
    }
}
