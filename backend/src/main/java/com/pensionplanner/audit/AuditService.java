package com.pensionplanner.audit;

import com.pensionplanner.income.OtherIncome;
import com.pensionplanner.income.OtherIncomeRepository;
import com.pensionplanner.income.StatePension;
import com.pensionplanner.income.StatePensionRepository;
import com.pensionplanner.pension.Pension;
import com.pensionplanner.pension.PensionRepository;
import com.pensionplanner.pension.PensionStatement;
import com.pensionplanner.pension.PensionStatementRepository;
import com.pensionplanner.user.UserSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AuditService {

    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";

    private final PensionAuditRepository pensionAuditRepository;
    private final PensionStatementAuditRepository pensionStatementAuditRepository;
    private final StatePensionAuditRepository statePensionAuditRepository;
    private final OtherIncomeAuditRepository otherIncomeAuditRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PensionRepository pensionRepository;
    private final PensionStatementRepository pensionStatementRepository;
    private final StatePensionRepository statePensionRepository;
    private final OtherIncomeRepository otherIncomeRepository;

    public AuditService(PensionAuditRepository pensionAuditRepository,
                        PensionStatementAuditRepository pensionStatementAuditRepository,
                        StatePensionAuditRepository statePensionAuditRepository,
                        OtherIncomeAuditRepository otherIncomeAuditRepository,
                        UserSettingsRepository userSettingsRepository,
                        PensionRepository pensionRepository,
                        PensionStatementRepository pensionStatementRepository,
                        StatePensionRepository statePensionRepository,
                        OtherIncomeRepository otherIncomeRepository) {
        this.pensionAuditRepository = pensionAuditRepository;
        this.pensionStatementAuditRepository = pensionStatementAuditRepository;
        this.statePensionAuditRepository = statePensionAuditRepository;
        this.otherIncomeAuditRepository = otherIncomeAuditRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.pensionRepository = pensionRepository;
        this.pensionStatementRepository = pensionStatementRepository;
        this.statePensionRepository = statePensionRepository;
        this.otherIncomeRepository = otherIncomeRepository;
    }

    @Transactional
    public void recordCreate(Pension pension) {
        if (!isAuditEnabled(pension.getUserId())) return;
        record(pension, ACTION_CREATE);
    }

    @Transactional
    public void recordUpdate(Pension pension) {
        if (!isAuditEnabled(pension.getUserId())) return;
        record(pension, ACTION_UPDATE);
    }

    @Transactional
    public void recordDelete(Pension pension) {
        if (!isAuditEnabled(pension.getUserId())) return;
        record(pension, ACTION_DELETE);
    }

    @Transactional
    public void recordCreate(Long userId, PensionStatement statement) {
        if (!isAuditEnabled(userId)) return;
        record(statement, ACTION_CREATE);
    }

    @Transactional
    public void recordUpdate(Long userId, PensionStatement statement) {
        if (!isAuditEnabled(userId)) return;
        record(statement, ACTION_UPDATE);
    }

    @Transactional
    public void recordDelete(Long userId, PensionStatement statement) {
        if (!isAuditEnabled(userId)) return;
        record(statement, ACTION_DELETE);
    }

    @Transactional
    public void recordCreate(StatePension statePension) {
        if (!isAuditEnabled(statePension.getUserId())) return;
        record(statePension, ACTION_CREATE);
    }

    @Transactional
    public void recordUpdate(StatePension statePension) {
        if (!isAuditEnabled(statePension.getUserId())) return;
        record(statePension, ACTION_UPDATE);
    }

    @Transactional
    public void recordDelete(StatePension statePension) {
        if (!isAuditEnabled(statePension.getUserId())) return;
        record(statePension, ACTION_DELETE);
    }

    @Transactional
    public void recordCreate(OtherIncome otherIncome) {
        if (!isAuditEnabled(otherIncome.getUserId())) return;
        record(otherIncome, ACTION_CREATE);
    }

    @Transactional
    public void recordUpdate(OtherIncome otherIncome) {
        if (!isAuditEnabled(otherIncome.getUserId())) return;
        record(otherIncome, ACTION_UPDATE);
    }

    @Transactional
    public void recordDelete(OtherIncome otherIncome) {
        if (!isAuditEnabled(otherIncome.getUserId())) return;
        record(otherIncome, ACTION_DELETE);
    }

    @Transactional
    public void purgeUserAudits(Long userId) {
        List<Long> pensionIds = pensionRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(Pension::getPensionId)
                .toList();
        pensionAuditRepository.deleteByUserId(userId);
        if (!pensionIds.isEmpty()) {
            pensionStatementAuditRepository.deleteByPensionIdIn(pensionIds);
        }
        statePensionAuditRepository.deleteByUserId(userId);
        otherIncomeAuditRepository.deleteByUserId(userId);
    }

    @Transactional
    public void snapshotUserAudits(Long userId) {
        for (Pension pension : pensionRepository.findByUserIdOrderByNameAsc(userId)) {
            record(pension, ACTION_CREATE);
            for (PensionStatement statement : pensionStatementRepository.findByPensionIdOrderByStatementDateAsc(pension.getPensionId())) {
                record(statement, ACTION_CREATE);
            }
        }
        statePensionRepository.findByUserId(userId)
                .ifPresent(statePension -> record(statePension, ACTION_CREATE));
        for (OtherIncome otherIncome : otherIncomeRepository.findByUserIdOrderByNameAsc(userId)) {
            record(otherIncome, ACTION_CREATE);
        }
    }

    private boolean isAuditEnabled(Long userId) {
        return userSettingsRepository.findByUserId(userId)
                .map(settings -> settings.isAuditEnabled())
                .orElse(true);
    }

    private void record(Pension pension, String action) {
        PensionAudit audit = new PensionAudit();
        audit.setAction(action);
        audit.setAuditTimestamp(Instant.now());
        audit.setPensionId(pension.getPensionId());
        audit.setUserId(pension.getUserId());
        audit.setName(pension.getName());
        audit.setMaturityDate(pension.getMaturityDate());
        audit.setNotes(pension.getNotes());
        audit.setStatus(pension.getStatus() == null ? null : pension.getStatus().name());
        audit.setStatusDate(pension.getStatusDate());
        audit.setColor(pension.getColor());
        audit.setProviderName(pension.getProviderName());
        audit.setPolicyNumber(pension.getPolicyNumber());
        audit.setWorkplaceName(pension.getWorkplaceName());
        pensionAuditRepository.save(audit);
    }

    private void record(PensionStatement statement, String action) {
        PensionStatementAudit audit = new PensionStatementAudit();
        audit.setAction(action);
        audit.setAuditTimestamp(Instant.now());
        audit.setStatementId(statement.getStatementId());
        audit.setPensionId(statement.getPensionId());
        audit.setStatementDate(statement.getStatementDate());
        audit.setPlanValue(statement.getPlanValue());
        audit.setProjectedAnnualAmount(statement.getProjectedAnnualAmount());
        audit.setYearlyCharges(statement.getYearlyCharges());
        audit.setTransferValue(statement.getTransferValue());
        audit.setAmountPaidIn(statement.getAmountPaidIn());
        audit.setStatementNotes(statement.getStatementNotes());
        pensionStatementAuditRepository.save(audit);
    }

    private void record(StatePension statePension, String action) {
        StatePensionAudit audit = new StatePensionAudit();
        audit.setAction(action);
        audit.setAuditTimestamp(Instant.now());
        audit.setId(statePension.getId());
        audit.setUserId(statePension.getUserId());
        audit.setName(statePension.getName());
        audit.setYearlyAmount(statePension.getYearlyAmount());
        audit.setTakesEffectYear(statePension.getTakesEffectYear());
        audit.setNotes(statePension.getNotes());
        statePensionAuditRepository.save(audit);
    }

    private void record(OtherIncome otherIncome, String action) {
        OtherIncomeAudit audit = new OtherIncomeAudit();
        audit.setAction(action);
        audit.setAuditTimestamp(Instant.now());
        audit.setId(otherIncome.getId());
        audit.setUserId(otherIncome.getUserId());
        audit.setName(otherIncome.getName());
        audit.setAnnualAmount(otherIncome.getAnnualAmount());
        audit.setNotes(otherIncome.getNotes());
        otherIncomeAuditRepository.save(audit);
    }
}
