package dev.jonclarke.pensionplanner.audit;

import dev.jonclarke.pensionplanner.common.ApiException;
import dev.jonclarke.pensionplanner.income.OtherIncomeRepository;
import dev.jonclarke.pensionplanner.income.StatePensionRepository;
import dev.jonclarke.pensionplanner.pension.PensionRepository;
import dev.jonclarke.pensionplanner.pension.PensionStatementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditReadService {

    private final PensionAuditRepository pensionAuditRepository;
    private final PensionStatementAuditRepository pensionStatementAuditRepository;
    private final OtherIncomeAuditRepository otherIncomeAuditRepository;
    private final StatePensionAuditRepository statePensionAuditRepository;
    private final PensionRepository pensionRepository;
    private final PensionStatementRepository pensionStatementRepository;
    private final OtherIncomeRepository otherIncomeRepository;
    private final StatePensionRepository statePensionRepository;

    public AuditReadService(PensionAuditRepository pensionAuditRepository,
                            PensionStatementAuditRepository pensionStatementAuditRepository,
                            OtherIncomeAuditRepository otherIncomeAuditRepository,
                            StatePensionAuditRepository statePensionAuditRepository,
                            PensionRepository pensionRepository,
                            PensionStatementRepository pensionStatementRepository,
                            OtherIncomeRepository otherIncomeRepository,
                            StatePensionRepository statePensionRepository) {
        this.pensionAuditRepository = pensionAuditRepository;
        this.pensionStatementAuditRepository = pensionStatementAuditRepository;
        this.otherIncomeAuditRepository = otherIncomeAuditRepository;
        this.statePensionAuditRepository = statePensionAuditRepository;
        this.pensionRepository = pensionRepository;
        this.pensionStatementRepository = pensionStatementRepository;
        this.otherIncomeRepository = otherIncomeRepository;
        this.statePensionRepository = statePensionRepository;
    }

    @Transactional(readOnly = true)
    public List<PensionAuditDto> getPensionAudit(Long userId, Long pensionId) {
        verifyPension(userId, pensionId);
        return pensionAuditRepository.findByPensionIdOrderByAuditTimestampDesc(pensionId)
                .stream().map(PensionAuditDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PensionStatementAuditDto> getStatementAudit(Long userId, Long statementId) {
        verifyStatement(userId, statementId);
        return pensionStatementAuditRepository.findByStatementIdOrderByAuditTimestampDesc(statementId)
                .stream().map(PensionStatementAuditDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<OtherIncomeAuditDto> getOtherIncomeAudit(Long userId, Long otherIncomeId) {
        verifyOtherIncome(userId, otherIncomeId);
        return otherIncomeAuditRepository.findByIdOrderByAuditTimestampDesc(otherIncomeId)
                .stream().map(OtherIncomeAuditDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<StatePensionAuditDto> getStatePensionAudit(Long userId, Long id) {
        verifyStatePension(userId, id);
        return statePensionAuditRepository.findByIdOrderByAuditTimestampDesc(id)
                .stream().map(StatePensionAuditDto::from).toList();
    }

    private void verifyPension(Long userId, Long pensionId) {
        pensionRepository.findByPensionIdAndUserId(pensionId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pension not found"));
    }

    private void verifyStatement(Long userId, Long statementId) {
        pensionStatementRepository.findByStatementIdAndUserId(statementId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Statement not found"));
    }

    private void verifyOtherIncome(Long userId, Long id) {
        otherIncomeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Other income not found"));
    }

    private void verifyStatePension(Long userId, Long id) {
        statePensionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "State pension not found"));
    }
}
