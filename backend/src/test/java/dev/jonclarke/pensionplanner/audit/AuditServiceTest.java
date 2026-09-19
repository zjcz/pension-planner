package dev.jonclarke.pensionplanner.audit;

import dev.jonclarke.pensionplanner.income.OtherIncome;
import dev.jonclarke.pensionplanner.income.OtherIncomeRepository;
import dev.jonclarke.pensionplanner.income.StatePension;
import dev.jonclarke.pensionplanner.income.StatePensionRepository;
import dev.jonclarke.pensionplanner.pension.Pension;
import dev.jonclarke.pensionplanner.pension.PensionRepository;
import dev.jonclarke.pensionplanner.pension.PensionStatus;
import dev.jonclarke.pensionplanner.pension.PensionStatement;
import dev.jonclarke.pensionplanner.pension.PensionStatementRepository;
import dev.jonclarke.pensionplanner.tag.OtherIncomeTag;
import dev.jonclarke.pensionplanner.tag.OtherIncomeTagRepository;
import dev.jonclarke.pensionplanner.tag.PensionTag;
import dev.jonclarke.pensionplanner.tag.PensionTagRepository;
import dev.jonclarke.pensionplanner.tag.Tag;
import dev.jonclarke.pensionplanner.tag.TagRepository;
import dev.jonclarke.pensionplanner.user.UserSettings;
import dev.jonclarke.pensionplanner.user.UserSettingsRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
class AuditServiceTest {

    private final PensionAuditRepository pensionAuditRepository = mock(PensionAuditRepository.class);
    private final PensionStatementAuditRepository statementAuditRepository = mock(PensionStatementAuditRepository.class);
    private final StatePensionAuditRepository statePensionAuditRepository = mock(StatePensionAuditRepository.class);
    private final OtherIncomeAuditRepository otherIncomeAuditRepository = mock(OtherIncomeAuditRepository.class);
    private final UserSettingsRepository userSettingsRepository = mock(UserSettingsRepository.class);
    private final PensionRepository pensionRepository = mock(PensionRepository.class);
    private final PensionStatementRepository pensionStatementRepository = mock(PensionStatementRepository.class);
    private final StatePensionRepository statePensionRepository = mock(StatePensionRepository.class);
    private final OtherIncomeRepository otherIncomeRepository = mock(OtherIncomeRepository.class);
    private final PensionTagRepository pensionTagRepository = mock(PensionTagRepository.class);
    private final OtherIncomeTagRepository otherIncomeTagRepository = mock(OtherIncomeTagRepository.class);
    private final TagRepository tagRepository = mock(TagRepository.class);

    private final AuditService auditService = new AuditService(
            pensionAuditRepository, statementAuditRepository, statePensionAuditRepository,
            otherIncomeAuditRepository, userSettingsRepository, pensionRepository,
            pensionStatementRepository, statePensionRepository, otherIncomeRepository,
            pensionTagRepository, otherIncomeTagRepository, tagRepository);

    private void enableAudit(Long userId) {
        UserSettings settings = new UserSettings();
        settings.setUserId(userId);
        settings.setAuditEnabled(true);
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
    }

    private void disableAudit(Long userId) {
        UserSettings settings = new UserSettings();
        settings.setUserId(userId);
        settings.setAuditEnabled(false);
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
    }

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

        PensionTag pensionTag = new PensionTag();
        pensionTag.setPensionId(7L);
        pensionTag.setTagId(1L);
        when(pensionTagRepository.findByPensionId(7L)).thenReturn(List.of(pensionTag));
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("ISA");
        when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(tag));

        enableAudit(3L);
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
        assertThat(audit.getTags()).isEqualTo("ISA");
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

        enableAudit(1L);
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

        enableAudit(5L);

        auditService.recordCreate(5L, statement);

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
        statePension.setYearlyAmount(1150000L);
        statePension.setTakesEffectYear(2026);
        statePension.setNotes("forecast");

        enableAudit(3L);
        auditService.recordUpdate(statePension);

        ArgumentCaptor<StatePensionAudit> captor = ArgumentCaptor.forClass(StatePensionAudit.class);
        verify(statePensionAuditRepository).save(captor.capture());
        StatePensionAudit audit = captor.getValue();
        assertThat(audit.getAction()).isEqualTo(AuditService.ACTION_UPDATE);
        assertThat(audit.getId()).isEqualTo(2L);
        assertThat(audit.getUserId()).isEqualTo(3L);
        assertThat(audit.getYearlyAmount()).isEqualTo(1150000L);
        assertThat(audit.getTakesEffectYear()).isEqualTo(2026);
    }

    @Test
    void otherIncomeSnapshotOnDelete() {
        OtherIncome otherIncome = new OtherIncome();
        otherIncome.setId(4L);
        otherIncome.setUserId(3L);
        otherIncome.setName("Rental");
        otherIncome.setAnnualAmount(600000L);

        OtherIncomeTag otherIncomeTag = new OtherIncomeTag();
        otherIncomeTag.setOtherIncomeId(4L);
        otherIncomeTag.setTagId(2L);
        when(otherIncomeTagRepository.findByOtherIncomeId(4L)).thenReturn(List.of(otherIncomeTag));
        Tag tag = new Tag();
        tag.setId(2L);
        tag.setName("Property");
        when(tagRepository.findAllById(List.of(2L))).thenReturn(List.of(tag));

        enableAudit(3L);
        auditService.recordDelete(otherIncome);

        ArgumentCaptor<OtherIncomeAudit> captor = ArgumentCaptor.forClass(OtherIncomeAudit.class);
        verify(otherIncomeAuditRepository).save(captor.capture());
        OtherIncomeAudit audit = captor.getValue();
        assertThat(audit.getAction()).isEqualTo(AuditService.ACTION_DELETE);
        assertThat(audit.getName()).isEqualTo("Rental");
        assertThat(audit.getAnnualAmount()).isEqualTo(600000L);
        assertThat(audit.getTags()).isEqualTo("Property");
    }

    @Test
    void pensionSkipsAuditWhenDisabled() {
        Pension pension = new Pension();
        pension.setPensionId(1L);
        pension.setUserId(4L);
        pension.setName("X");
        pension.setMaturityDate(LocalDate.of(2030, 1, 1));
        pension.setStatus(PensionStatus.ACTIVE);

        disableAudit(4L);
        auditService.recordCreate(pension);

        verify(pensionAuditRepository, never()).save(any());
    }

    @Test
    void statementSkipsAuditWhenDisabled() {
        PensionStatement statement = new PensionStatement();
        statement.setStatementId(1L);
        statement.setPensionId(10L);
        statement.setStatementDate(LocalDate.of(2026, 1, 1));
        statement.setPlanValue(100L);
        statement.setProjectedAnnualAmount(50L);

        disableAudit(4L);

        auditService.recordDelete(4L, statement);

        verify(statementAuditRepository, never()).save(any());
    }

    @Test
    void purgeDeletesAllAuditTypesForUser() {
        Pension pension = new Pension();
        pension.setPensionId(7L);
        pension.setUserId(3L);
        when(pensionRepository.findByUserIdOrderByNameAsc(3L)).thenReturn(List.of(pension));

        auditService.purgeUserAudits(3L);

        verify(pensionAuditRepository).deleteByUserId(3L);
        verify(statementAuditRepository).deleteByPensionIdIn(List.of(7L));
        verify(statePensionAuditRepository).deleteByUserId(3L);
        verify(otherIncomeAuditRepository).deleteByUserId(3L);
    }

    @Test
    void purgeHandlesUserWithNoPensions() {
        when(pensionRepository.findByUserIdOrderByNameAsc(3L)).thenReturn(List.of());

        auditService.purgeUserAudits(3L);

        verify(pensionAuditRepository).deleteByUserId(3L);
        verify(statementAuditRepository, never()).deleteByPensionIdIn(any());
    }

    @Test
    void snapshotWritesCreateForEveryRecord() {
        Pension pension = new Pension();
        pension.setPensionId(7L);
        pension.setUserId(3L);
        pension.setName("P1");
        pension.setMaturityDate(LocalDate.of(2040, 1, 1));
        pension.setStatus(PensionStatus.ACTIVE);

        PensionStatement statement = new PensionStatement();
        statement.setStatementId(1L);
        statement.setPensionId(7L);
        statement.setStatementDate(LocalDate.of(2026, 1, 1));
        statement.setPlanValue(100000L);
        statement.setProjectedAnnualAmount(5000L);

        StatePension statePension = new StatePension();
        statePension.setId(2L);
        statePension.setUserId(3L);
        statePension.setName("State");
        statePension.setYearlyAmount(1000000L);

        OtherIncome otherIncome = new OtherIncome();
        otherIncome.setId(4L);
        otherIncome.setUserId(3L);
        otherIncome.setName("Rental");
        otherIncome.setAnnualAmount(600000L);

        when(pensionRepository.findByUserIdOrderByNameAsc(3L)).thenReturn(List.of(pension));
        when(pensionStatementRepository.findByPensionIdOrderByStatementDateAsc(7L)).thenReturn(List.of(statement));
        when(statePensionRepository.findByUserIdOrderByNameAsc(3L)).thenReturn(List.of(statePension));
        when(otherIncomeRepository.findByUserIdOrderByNameAsc(3L)).thenReturn(List.of(otherIncome));

        auditService.snapshotUserAudits(3L);

        ArgumentCaptor<PensionAudit> pensionCaptor = ArgumentCaptor.forClass(PensionAudit.class);
        verify(pensionAuditRepository).save(pensionCaptor.capture());
        assertThat(pensionCaptor.getValue().getAction()).isEqualTo(AuditService.ACTION_CREATE);
        assertThat(pensionCaptor.getValue().getPensionId()).isEqualTo(7L);

        ArgumentCaptor<PensionStatementAudit> stmtCaptor = ArgumentCaptor.forClass(PensionStatementAudit.class);
        verify(statementAuditRepository).save(stmtCaptor.capture());
        assertThat(stmtCaptor.getValue().getPensionId()).isEqualTo(7L);

        ArgumentCaptor<StatePensionAudit> spCaptor = ArgumentCaptor.forClass(StatePensionAudit.class);
        verify(statePensionAuditRepository).save(spCaptor.capture());
        assertThat(spCaptor.getValue().getUserId()).isEqualTo(3L);

        ArgumentCaptor<OtherIncomeAudit> oiCaptor = ArgumentCaptor.forClass(OtherIncomeAudit.class);
        verify(otherIncomeAuditRepository).save(oiCaptor.capture());
        assertThat(oiCaptor.getValue().getName()).isEqualTo("Rental");
    }
}
