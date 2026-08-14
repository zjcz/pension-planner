package com.pensionplanner.audit;

import com.pensionplanner.income.OtherIncome;
import com.pensionplanner.income.StatePension;
import com.pensionplanner.pension.Pension;
import com.pensionplanner.pension.PensionStatus;
import com.pensionplanner.pension.PensionStatement;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;

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
        pension.setMaturityDate(LocalDate.of(2040, 1, 1));
        pension.setNotes("some notes");
        pension.setStatus(PensionStatus.ACTIVE);
        pension.setStatusDate(LocalDate.of(2026, 1, 1));
        pension.setColor("#FF5733");

        auditService.recordCreate(pension);

        ArgumentCaptor<PensionAudit> captor = ArgumentCaptor.forClass(PensionAudit.class);
        verify(pensionAuditRepository).save(captor.capture());
        PensionAudit audit = captor.getValue();
        assertThat(audit.getAction()).isEqualTo(AuditService.ACTION_CREATE);
        assertThat(audit.getPensionId()).isEqualTo(7L);
        assertThat(audit.getUserId()).isEqualTo(3L);
        assertThat(audit.getName()).isEqualTo("My Pension");
        assertThat(audit.getMaturityDate()).isEqualTo(LocalDate.of(2040, 1, 1));
        assertThat(audit.getNotes()).isEqualTo("some notes");
        assertThat(audit.getStatus()).isEqualTo("ACTIVE");
        assertThat(audit.getStatusDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(audit.getColor()).isEqualTo("#FF5733");
        assertThat(audit.getAuditTimestamp()).isNotNull();
    }

    @Test
    void pensionSnapshotOnUpdateAndDelete() {
        Pension pension = new Pension();
        pension.setPensionId(9L);
        pension.setUserId(1L);
        pension.setName("Old Name");
        pension.setMaturityDate(LocalDate.of(2030, 1, 1));
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
        statement.setStatementDate(LocalDate.of(2026, 3, 31));
        statement.setPlanValue(10000000L);
        statement.setProjectedAnnualAmount(450000L);
        statement.setYearlyCharges(50000L);
        statement.setTransferValue(9800000L);
        statement.setAmountPaidIn(4000000L);
        statement.setStatementNotes("March statement");

        auditService.recordCreate(statement);

        ArgumentCaptor<PensionStatementAudit> captor = ArgumentCaptor.forClass(PensionStatementAudit.class);
        verify(statementAuditRepository).save(captor.capture());
        PensionStatementAudit audit = captor.getValue();
        assertThat(audit.getAction()).isEqualTo(AuditService.ACTION_CREATE);
        assertThat(audit.getStatementId()).isEqualTo(11L);
        assertThat(audit.getPensionId()).isEqualTo(7L);
        assertThat(audit.getPlanValue()).isEqualTo(10000000L);
        assertThat(audit.getProjectedAnnualAmount()).isEqualTo(450000L);
        assertThat(audit.getYearlyCharges()).isEqualTo(50000L);
        assertThat(audit.getAmountPaidIn()).isEqualTo(4000000L);
    }

    @Test
    void statePensionSnapshotOnUpdate() {
        StatePension statePension = new StatePension();
        statePension.setId(2L);
        statePension.setUserId(3L);
        statePension.setName("State Pension");
        statePension.setAnnualAmount(1150000L);
        statePension.setNotes("forecast");

        auditService.recordUpdate(statePension);

        ArgumentCaptor<StatePensionAudit> captor = ArgumentCaptor.forClass(StatePensionAudit.class);
        verify(statePensionAuditRepository).save(captor.capture());
        StatePensionAudit audit = captor.getValue();
        assertThat(audit.getAction()).isEqualTo(AuditService.ACTION_UPDATE);
        assertThat(audit.getId()).isEqualTo(2L);
        assertThat(audit.getUserId()).isEqualTo(3L);
        assertThat(audit.getAnnualAmount()).isEqualTo(1150000L);
    }

    @Test
    void otherIncomeSnapshotOnDelete() {
        OtherIncome otherIncome = new OtherIncome();
        otherIncome.setId(4L);
        otherIncome.setUserId(3L);
        otherIncome.setName("Rental");
        otherIncome.setAnnualAmount(600000L);

        auditService.recordDelete(otherIncome);

        ArgumentCaptor<OtherIncomeAudit> captor = ArgumentCaptor.forClass(OtherIncomeAudit.class);
        verify(otherIncomeAuditRepository).save(captor.capture());
        OtherIncomeAudit audit = captor.getValue();
        assertThat(audit.getAction()).isEqualTo(AuditService.ACTION_DELETE);
        assertThat(audit.getName()).isEqualTo("Rental");
        assertThat(audit.getAnnualAmount()).isEqualTo(600000L);
    }
}
