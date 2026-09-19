package dev.jonclarke.pensionplanner.pension;

import dev.jonclarke.pensionplanner.audit.AuditService;
import dev.jonclarke.pensionplanner.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StatementService {

    private final PensionRepository pensionRepository;
    private final PensionStatementRepository statementRepository;
    private final AuditService auditService;

    public StatementService(PensionRepository pensionRepository,
                            PensionStatementRepository statementRepository,
                            AuditService auditService) {
        this.pensionRepository = pensionRepository;
        this.statementRepository = statementRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<PensionStatement> listForPension(Long userId, Long pensionId) {
        verifyPensionOwnership(userId, pensionId);
        return statementRepository.findByPensionIdOrderByStatementDateAsc(pensionId);
    }

    @Transactional(readOnly = true)
    public PensionStatement getForUser(Long userId, Long pensionId, Long statementId) {
        verifyPensionOwnership(userId, pensionId);
        return statementRepository.findByStatementIdAndPensionId(statementId, pensionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Statement not found"));
    }

    @Transactional
    public PensionStatement create(Long userId, Long pensionId, StatementRequest request) {
        verifyPensionOwnership(userId, pensionId);
        PensionStatement statement = new PensionStatement();
        statement.setPensionId(pensionId);
        apply(statement, request);
        PensionStatement saved = statementRepository.save(statement);
        auditService.recordCreate(userId, saved);
        return saved;
    }

    @Transactional
    public PensionStatement update(Long userId, Long pensionId, Long statementId, StatementRequest request) {
        PensionStatement statement = getForUser(userId, pensionId, statementId);
        auditService.recordUpdate(userId, statement);
        apply(statement, request);
        return statementRepository.save(statement);
    }

    @Transactional
    public void delete(Long userId, Long pensionId, Long statementId) {
        PensionStatement statement = getForUser(userId, pensionId, statementId);
        auditService.recordDelete(userId, statement);
        statementRepository.delete(statement);
    }

    private void verifyPensionOwnership(Long userId, Long pensionId) {
        pensionRepository.findByPensionIdAndUserId(pensionId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pension not found"));
    }

    private void apply(PensionStatement statement, StatementRequest request) {
        statement.setStatementDate(request.statementDate());
        statement.setPlanValue(request.planValue());
        statement.setProjectedAnnualAmount(request.projectedAnnualAmount());
        statement.setYearlyCharges(request.yearlyCharges());
        statement.setTransferValue(request.transferValue());
        statement.setAmountPaidIn(request.amountPaidIn());
        statement.setStatementNotes(request.statementNotes());
    }
}
