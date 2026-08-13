package com.pensionplanner.audit;

import com.pensionplanner.income.OtherIncome;
import com.pensionplanner.income.StatePension;
import com.pensionplanner.pension.Pension;
import com.pensionplanner.pension.PensionStatus;
import com.pensionplanner.pension.PensionStatement;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuditServiceTest {

    private final PensionAuditRepository pensionAuditRepository = mock(PensionAuditRepository.class);
    private final PensionStatementAuditRepository statementAuditRepository = mock(PensionStatementAuditRepository.class);
    private final StatePensionAuditRepository statePensionAuditRepository = mock(StatePensionAuditRepository.class);
    private final OtherIncomeAuditRepository otherIncomeAuditRepository = mock(OtherIncomeAuditRepository.class);

    private final AuditService auditService = new AuditService(
            pensionAuditRepository, statementAuditRepository, statePensionAuditRepository, otherIncomeAuditRepository);

    @Test
    void pensionSnapshotOnCreate() {
        Pension pension = new Pension();
        pension.setPensionId(7L);
        pension.setUserId(3L);
        pension.setName("My Pension");
        pension.setMaturityDate(Instant.parse("2040-01-01T00:00:00Z"));
        pension.setNotes("some notes");
        pension.setStatus(PensionStatus.ACTIVE);
        pension.setStatusDate(Instant.parse("2026-01-01T00:00:00Z"));
        pension.setColor("#FF5733");

        auditService.recordCreate(pension);

        ArgumentCaptor<PensionAudit> captor = ArgumentCaptor.forClass(PensionAudit.class);
        verify(pensionAuditRepository).save(captor.capture());
        PensionAudit audit = captor.getValue();
        assertThat(audit.getAction()).isEqualTo(AuditService.ACTION_CREATE);
        assertThat(audit.getPensionId()).isEqualTo(7L);
        assertThat(audit.getUserId()).isEqualTo(3L);
        assertThat(audit.getName()).isEqualTo("My Pension");
        assertThat(audit.getMaturityDate()).isEqualTo(Instant.parse("2040-01-01T00:00:00Z"));
        assertThat(audit.getNotes()).isEqualTo("some notes");
        assertThat(audit.getStatus()).isEqualTo("ACTIVE");
        assertThat(audit.getStatusDate()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(audit.getColor()).isEqualTo("#FF5733");
        assertThat(audit.getAuditTimestamp()).isNotNull();
    }

    @Test
    void pensionSnapshotOnUpdateAndDelete() {
        Pension pension = new Pension();
        pension.setPensionId(9L);
        pension.setUserId(1L);
        pension.setName("Old Name");
        pension.setMaturityDate(Instant.parse("2030-01-01T00:00:00Z"));
        pension.setStatus(PensionStatus.CLOSED);

        auditService.recordUpdate(pension);
        auditService.recordDelete(pension);

        ArgumentCaptor<PensionAudit> captor = ArgumentCaptor.forClass(PensionAudit.class);
        verify(pensionAuditRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(PensionAudit::getAction)
                .containsExactly(AuditService.ACTION_UPDATE, AuditService.ACTION_DELETE);
        assertThat(captor.getAllValues()).extracting(PensionAudit::getName)
                .containsExactly("Old Name", "Old Name");
    }

    @Test
    void statementSnapshotOnCreate() {
        PensionStatement statement = new PensionStatement();
        statement.setStatementId(11L);
        statement.setPensionId(7L);
        statement.setUserId(3L);
        statement.setStatementDate(Instant.parse("2026-03-31T00:00:00Z"));
        statement.setPlanValue(100000.0);
        statement.setProjectedAnnualAmount(4500.0);
        statement.setYearlyCharges(500.0);
        statement.setTransferValue(98000.0);
        statement.setAmountPaidIn(40000.0);
        statement.setStatementNotes("March statement");

        auditService.recordCreate(statement);

        ArgumentCaptor<PensionStatementAudit> captor = ArgumentCaptor.forClass(PensionStatementAudit.class);
        verify(statementAuditRepository).save(captor.capture());
        PensionStatementAudit audit = captor.getValue();
        assertThat(audit.getAction()).isEqualTo(AuditService.ACTION_CREATE);
        assertThat(audit.getStatementId()).isEqualTo(11L);
        assertThat(audit.getPensionId()).isEqualTo(7L);
        assertThat(audit.getPlanValue()).isEqualTo(100000.0);
        assertThat(audit.getProjectedAnnualAmount()).isEqualTo(4500.0);
        assertThat(audit.getYearlyCharges()).isEqualTo(500.0);
        assertThat(audit.getAmountPaidIn()).isEqualTo(40000.0);
    }

    @Test
    void statePensionSnapshotOnUpdate() {
        StatePension statePension = new StatePension();
        statePension.setId(2L);
        statePension.setUserId(3L);
        statePension.setName("State Pension");
        statePension.setAnnualAmount(11500.0);
        statePension.setNotes("forecast");

        auditService.recordUpdate(statePension);

        ArgumentCaptor<StatePensionAudit> captor = ArgumentCaptor.forClass(StatePensionAudit.class);
        verify(statePensionAuditRepository).save(captor.capture());
        StatePensionAudit audit = captor.getValue();
        assertThat(audit.getAction()).isEqualTo(AuditService.ACTION_UPDATE);
        assertThat(audit.getId()).isEqualTo(2L);
        assertThat(audit.getUserId()).isEqualTo(3L);
        assertThat(audit.getAnnualAmount()).isEqualTo(11500.0);
    }

    @Test
    void otherIncomeSnapshotOnDelete() {
        OtherIncome otherIncome = new OtherIncome();
        otherIncome.setId(4L);
        otherIncome.setUserId(3L);
        otherIncome.setName("Rental");
        otherIncome.setAnnualAmount(6000.0);

        auditService.recordDelete(otherIncome);

        ArgumentCaptor<OtherIncomeAudit> captor = ArgumentCaptor.forClass(OtherIncomeAudit.class);
        verify(otherIncomeAuditRepository).save(captor.capture());
        OtherIncomeAudit audit = captor.getValue();
        assertThat(audit.getAction()).isEqualTo(AuditService.ACTION_DELETE);
        assertThat(audit.getName()).isEqualTo("Rental");
        assertThat(audit.getAnnualAmount()).isEqualTo(6000.0);
    }
}
