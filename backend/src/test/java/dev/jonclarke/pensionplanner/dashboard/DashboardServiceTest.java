package dev.jonclarke.pensionplanner.dashboard;

import dev.jonclarke.pensionplanner.income.OtherIncome;
import dev.jonclarke.pensionplanner.income.OtherIncomeRepository;
import dev.jonclarke.pensionplanner.income.StatePension;
import dev.jonclarke.pensionplanner.income.StatePensionRepository;
import dev.jonclarke.pensionplanner.pension.Pension;
import dev.jonclarke.pensionplanner.pension.PensionRepository;
import dev.jonclarke.pensionplanner.pension.PensionStatement;
import dev.jonclarke.pensionplanner.pension.PensionStatementRepository;
import dev.jonclarke.pensionplanner.pension.PensionStatus;
import dev.jonclarke.pensionplanner.user.UserSettings;
import dev.jonclarke.pensionplanner.user.UserSettingsRepository;
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
        when(statePensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());
        when(otherIncomeRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());
        when(userSettingsRepository.findByUserId(42L)).thenReturn(Optional.empty());

        DashboardDto result = service.getForUser(42L);

        assertThat(result.totalPortfolioValue()).isEqualTo(0L);
        assertThat(result.totalProjectedAnnualIncome()).isEqualTo(0L);
        assertThat(result.targetIncome()).isNull();
        assertThat(result.statePensions()).isEmpty();
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
        when(statePensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());
        when(otherIncomeRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());
        when(userSettingsRepository.findByUserId(42L)).thenReturn(Optional.empty());

        DashboardDto result = service.getForUser(42L);

        assertThat(result.totalPortfolioValue()).isEqualTo(150000L);
        assertThat(result.totalProjectedAnnualIncome()).isEqualTo(7500L);
    }

    @Test
    void includesAllStatePensionsAndOtherIncome() {
        when(pensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());

        StatePension sp1 = new StatePension();
        sp1.setId(1L);
        sp1.setName("Mine");
        sp1.setYearlyAmount(10000L);
        sp1.setTakesEffectYear(2028);
        StatePension sp2 = new StatePension();
        sp2.setId(2L);
        sp2.setName("Partner");
        sp2.setYearlyAmount(6000L);
        sp2.setTakesEffectYear(2030);
        when(statePensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of(sp1, sp2));

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

        assertThat(result.totalProjectedAnnualIncome()).isEqualTo(21000L);
        assertThat(result.statePensions()).hasSize(2);
        assertThat(result.statePensions().get(0).name()).isEqualTo("Mine");
        assertThat(result.statePensions().get(1).yearlyAmount()).isEqualTo(6000L);
        assertThat(result.otherIncome()).hasSize(1);
        assertThat(result.targetIncome()).isEqualTo(30000L);
        assertThat(result.retirementDate()).isEqualTo(LocalDate.of(2035, 12, 31));
    }
}
