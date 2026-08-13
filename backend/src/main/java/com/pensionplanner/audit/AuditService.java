package com.pensionplanner.audit;

import com.pensionplanner.income.OtherIncome;
import com.pensionplanner.income.StatePension;
import com.pensionplanner.pension.Pension;
import com.pensionplanner.pension.PensionStatement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuditService {

    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";

    private final PensionAuditRepository pensionAuditRepository;
    private final PensionStatementAuditRepository pensionStatementAuditRepository;
    private final StatePensionAuditRepository statePensionAuditRepository;
    private final OtherIncomeAuditRepository otherIncomeAuditRepository;

    public AuditService(PensionAuditRepository pensionAuditRepository,
                        PensionStatementAuditRepository pensionStatementAuditRepository,
                        StatePensionAuditRepository statePensionAuditRepository,
                        OtherIncomeAuditRepository otherIncomeAuditRepository) {
        this.pensionAuditRepository = pensionAuditRepository;
        this.pensionStatementAuditRepository = pensionStatementAuditRepository;
        this.statePensionAuditRepository = statePensionAuditRepository;
        this.otherIncomeAuditRepository = otherIncomeAuditRepository;
    }

    @Transactional
    public void recordCreate(Pension pension) {
        record(pension, ACTION_CREATE);
    }

    @Transactional
    public void recordUpdate(Pension pension) {
        record(pension, ACTION_UPDATE);
    }

    @Transactional
    public void recordDelete(Pension pension) {
        record(pension, ACTION_DELETE);
    }

    @Transactional
    public void recordCreate(PensionStatement statement) {
        record(statement, ACTION_CREATE);
    }

    @Transactional
    public void recordUpdate(PensionStatement statement) {
        record(statement, ACTION_UPDATE);
    }

    @Transactional
    public void recordDelete(PensionStatement statement) {
        record(statement, ACTION_DELETE);
    }

    @Transactional
    public void recordCreate(StatePension statePension) {
        record(statePension, ACTION_CREATE);
    }

    @Transactional
    public void recordUpdate(StatePension statePension) {
        record(statePension, ACTION_UPDATE);
    }

    @Transactional
    public void recordDelete(StatePension statePension) {
        record(statePension, ACTION_DELETE);
    }

    @Transactional
    public void recordCreate(OtherIncome otherIncome) {
        record(otherIncome, ACTION_CREATE);
    }

    @Transactional
    public void recordUpdate(OtherIncome otherIncome) {
        record(otherIncome, ACTION_UPDATE);
    }

    @Transactional
    public void recordDelete(OtherIncome otherIncome) {
        record(otherIncome, ACTION_DELETE);
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
        pensionAuditRepository.save(audit);
    }

    private void record(PensionStatement statement, String action) {
        PensionStatementAudit audit = new PensionStatementAudit();
        audit.setAction(action);
        audit.setAuditTimestamp(Instant.now());
        audit.setStatementId(statement.getStatementId());
        audit.setPensionId(statement.getPensionId());
        audit.setUserId(statement.getUserId());
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
        audit.setAnnualAmount(statePension.getAnnualAmount());
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
