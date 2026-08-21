package com.pensionplanner.dashboard;

import com.pensionplanner.income.OtherIncome;
import com.pensionplanner.income.OtherIncomeRepository;
import com.pensionplanner.income.StatePension;
import com.pensionplanner.income.StatePensionRepository;
import com.pensionplanner.pension.Pension;
import com.pensionplanner.pension.PensionRepository;
import com.pensionplanner.pension.PensionStatement;
import com.pensionplanner.pension.PensionStatementRepository;
import com.pensionplanner.pension.PensionStatus;
import com.pensionplanner.user.UserSettings;
import com.pensionplanner.user.UserSettingsRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private final PensionRepository pensionRepository = mock(PensionRepository.class);
    private final PensionStatementRepository statementRepository = mock(PensionStatementRepository.class);
    private final StatePensionRepository statePensionRepository = mock(StatePensionRepository.class);
    private final OtherIncomeRepository otherIncomeRepository = mock(OtherIncomeRepository.class);
    private final UserSettingsRepository userSettingsRepository = mock(UserSettingsRepository.class);
    private final DashboardService service = new DashboardService(
            pensionRepository, statementRepository, statePensionRepository,
            otherIncomeRepository, userSettingsRepository);

    @Test
    void emptyDataReturnsZeroesAndNulls() {
        when(pensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());
        when(statePensionRepository.findByUserId(42L)).thenReturn(Optional.empty());
        when(otherIncomeRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());
        when(userSettingsRepository.findByUserId(42L)).thenReturn(Optional.empty());

        DashboardDto result = service.getForUser(42L);

        assertThat(result.totalPortfolioValue()).isEqualTo(0L);
        assertThat(result.totalProjectedAnnualIncome()).isEqualTo(0L);
        assertThat(result.targetIncome()).isNull();
        assertThat(result.statePension()).isNull();
        assertThat(result.otherIncome()).isEmpty();
        assertThat(result.retirementDate()).isNull();
    }

    @Test
    void aggregatesActivePensionsLatestStatements() {
        Pension active = new Pension();
        active.setPensionId(1L);
        active.setStatus(PensionStatus.ACTIVE);

        Pension closed = new Pension();
        closed.setPensionId(2L);
        closed.setStatus(PensionStatus.CLOSED);

        when(pensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of(active, closed));

        PensionStatement stmt1 = new PensionStatement();
        stmt1.setPlanValue(100000L);
        stmt1.setProjectedAnnualAmount(5000L);
        stmt1.setStatementDate(LocalDate.of(2025, 6, 1));

        PensionStatement stmt2 = new PensionStatement();
        stmt2.setPlanValue(150000L);
        stmt2.setProjectedAnnualAmount(7500L);
        stmt2.setStatementDate(LocalDate.of(2026, 1, 1));

        when(statementRepository.findByPensionIdOrderByStatementDateAsc(1L)).thenReturn(List.of(stmt1, stmt2));
        when(statementRepository.findByPensionIdOrderByStatementDateAsc(2L)).thenReturn(List.of());
        when(statePensionRepository.findByUserId(42L)).thenReturn(Optional.empty());
        when(otherIncomeRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());
        when(userSettingsRepository.findByUserId(42L)).thenReturn(Optional.empty());

        DashboardDto result = service.getForUser(42L);

        assertThat(result.totalPortfolioValue()).isEqualTo(150000L);
        assertThat(result.totalProjectedAnnualIncome()).isEqualTo(7500L);
    }

    @Test
    void includesStatePensionAndOtherIncome() {
        when(pensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());

        StatePension sp = new StatePension();
        sp.setYearlyAmount(10000L);
        sp.setTakesEffectYear(2028);
        when(statePensionRepository.findByUserId(42L)).thenReturn(Optional.of(sp));

        OtherIncome oi = new OtherIncome();
        oi.setId(1L);
        oi.setName("Rental");
        oi.setAnnualAmount(5000L);
        when(otherIncomeRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of(oi));

        UserSettings settings = new UserSettings();
        settings.setTargetIncome(30000L);
        settings.setRetirementDate(LocalDate.of(2035, 12, 31));
        when(userSettingsRepository.findByUserId(42L)).thenReturn(Optional.of(settings));

        DashboardDto result = service.getForUser(42L);

        assertThat(result.totalProjectedAnnualIncome()).isEqualTo(15000L);
        assertThat(result.statePension().yearlyAmount()).isEqualTo(10000L);
        assertThat(result.otherIncome()).hasSize(1);
        assertThat(result.targetIncome()).isEqualTo(30000L);
        assertThat(result.retirementDate()).isEqualTo(LocalDate.of(2035, 12, 31));
    }
}
