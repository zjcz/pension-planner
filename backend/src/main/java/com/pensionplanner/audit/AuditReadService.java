package com.pensionplanner.audit;

import com.pensionplanner.common.ApiException;
import com.pensionplanner.income.OtherIncomeRepository;
import com.pensionplanner.pension.PensionRepository;
import com.pensionplanner.pension.PensionStatementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditReadService {

    private final PensionAuditRepository pensionAuditRepository;
    private final PensionStatementAuditRepository pensionStatementAuditRepository;
    private final OtherIncomeAuditRepository otherIncomeAuditRepository;
    private final PensionRepository pensionRepository;
    private final PensionStatementRepository pensionStatementRepository;
    private final OtherIncomeRepository otherIncomeRepository;

    public AuditReadService(PensionAuditRepository pensionAuditRepository,
                            PensionStatementAuditRepository pensionStatementAuditRepository,
                            OtherIncomeAuditRepository otherIncomeAuditRepository,
                            PensionRepository pensionRepository,
                            PensionStatementRepository pensionStatementRepository,
                            OtherIncomeRepository otherIncomeRepository) {
        this.pensionAuditRepository = pensionAuditRepository;
        this.pensionStatementAuditRepository = pensionStatementAuditRepository;
        this.otherIncomeAuditRepository = otherIncomeAuditRepository;
        this.pensionRepository = pensionRepository;
        this.pensionStatementRepository = pensionStatementRepository;
        this.otherIncomeRepository = otherIncomeRepository;
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
}
