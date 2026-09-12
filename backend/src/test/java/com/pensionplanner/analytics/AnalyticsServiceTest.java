package com.pensionplanner.analytics;

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

class AnalyticsServiceTest {

    private final PensionRepository pensionRepository = mock(PensionRepository.class);
    private final PensionStatementRepository statementRepository = mock(PensionStatementRepository.class);
    private final StatePensionRepository statePensionRepository = mock(StatePensionRepository.class);
    private final OtherIncomeRepository otherIncomeRepository = mock(OtherIncomeRepository.class);
    private final UserSettingsRepository userSettingsRepository = mock(UserSettingsRepository.class);
    private final AnalyticsService service = new AnalyticsService(
            pensionRepository, statementRepository, statePensionRepository,
            otherIncomeRepository, userSettingsRepository);

    @Test
    void emptyDataReturnsEmptyAnalytics() {
        when(pensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());
        when(statePensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());
        when(otherIncomeRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());
        when(userSettingsRepository.findByUserId(42L)).thenReturn(Optional.empty());

        AnalyticsDto result = service.getForUser(42L);

        assertThat(result.today()).isEqualTo(LocalDate.now());
        assertThat(result.retirementDate()).isNull();
        assertThat(result.targetIncome()).isNull();
        assertThat(result.projections()).isEmpty();
        assertThat(result.pensionGrowthCosts()).isEmpty();
        assertThat(result.pensionIncomeBreakdown()).isEmpty();
        assertThat(result.statePensionBreakdown()).isEmpty();
        assertThat(result.otherIncomeBreakdown()).isEmpty();
        assertThat(result.pensionHistorySeries()).isEmpty();
    }

    @Test
    void projectsGrowthToRetirementDate() {
        Pension active = new Pension();
        active.setPensionId(1L);
        active.setStatus(PensionStatus.ACTIVE);
        when(pensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of(active));

        PensionStatement stmt = new PensionStatement();
        stmt.setPlanValue(100000L);
        stmt.setProjectedAnnualAmount(10000L);
        stmt.setStatementDate(LocalDate.of(2026, 1, 1));
        when(statementRepository.findByPensionIdOrderByStatementDateAsc(1L)).thenReturn(List.of(stmt));

        UserSettings settings = new UserSettings();
        settings.setRetirementDate(LocalDate.of(2030, 12, 31));
        settings.setTargetIncome(30000L);
        when(userSettingsRepository.findByUserId(42L)).thenReturn(Optional.of(settings));
        when(statePensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());
        when(otherIncomeRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());

        AnalyticsDto result = service.getForUser(42L);

        assertThat(result.projections()).hasSize(5); // 2026, 2027, 2028, 2029, 2030
        assertThat(result.projections().get(0).totalValue()).isEqualTo(100000L);
        assertThat(result.projections().get(1).totalValue()).isEqualTo(110000L);
        assertThat(result.projections().get(4).totalValue()).isEqualTo(140000L);
    }

    @Test
    void computesGrowthVsCost() {
        Pension p = new Pension();
        p.setPensionId(1L);
        p.setName("Test");
        p.setColor("#FF0000");
        p.setStatus(PensionStatus.ACTIVE);
        when(pensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of(p));

        PensionStatement s1 = new PensionStatement();
        s1.setPlanValue(50000L);
        s1.setAmountPaidIn(40000L);
        s1.setYearlyCharges(500L);
        s1.setStatementDate(LocalDate.of(2025, 1, 1));

        PensionStatement s2 = new PensionStatement();
        s2.setPlanValue(60000L);
        s2.setAmountPaidIn(50000L);
        s2.setYearlyCharges(600L);
        s2.setStatementDate(LocalDate.of(2026, 1, 1));

        when(statementRepository.findByPensionIdOrderByStatementDateAsc(1L)).thenReturn(List.of(s1, s2));
        when(statePensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());
        when(otherIncomeRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());
        when(userSettingsRepository.findByUserId(42L)).thenReturn(Optional.empty());

        AnalyticsDto result = service.getForUser(42L);

        assertThat(result.pensionGrowthCosts()).hasSize(1);
        AnalyticsDto.PensionGrowthCost gc = result.pensionGrowthCosts().get(0);
        assertThat(gc.growthValue()).isEqualTo(-30000L); // 60000 - (40000 + 50000)
        assertThat(gc.cumulativeCharges()).isEqualTo(1100L); // 500 + 600
    }

    @Test
    void aggregatesIncomeBreakdown() {
        Pension active = new Pension();
        active.setPensionId(1L);
        active.setStatus(PensionStatus.ACTIVE);
        when(pensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of(active));

        PensionStatement stmt = new PensionStatement();
        stmt.setPlanValue(100000L);
        stmt.setProjectedAnnualAmount(8000L);
        stmt.setStatementDate(LocalDate.of(2026, 1, 1));
        when(statementRepository.findByPensionIdOrderByStatementDateAsc(1L)).thenReturn(List.of(stmt));

        StatePension sp1 = new StatePension();
        sp1.setName("Mine");
        sp1.setYearlyAmount(11000L);
        StatePension sp2 = new StatePension();
        sp2.setName("Partner");
        sp2.setYearlyAmount(5000L);
        when(statePensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of(sp1, sp2));

        OtherIncome oi = new OtherIncome();
        oi.setName("Rental");
        oi.setAnnualAmount(3000L);
        when(otherIncomeRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of(oi));

        when(userSettingsRepository.findByUserId(42L)).thenReturn(Optional.empty());

        AnalyticsDto result = service.getForUser(42L);

        assertThat(result.pensionIncomeBreakdown()).hasSize(1);
        assertThat(result.pensionIncomeBreakdown().get(0).projectedAnnualAmount()).isEqualTo(8000L);
        assertThat(result.statePensionBreakdown()).hasSize(2);
        assertThat(result.statePensionBreakdown().get(0).name()).isEqualTo("Mine");
        assertThat(result.statePensionBreakdown().get(0).yearlyAmount()).isEqualTo(11000L);
        assertThat(result.statePensionBreakdown().get(1).yearlyAmount()).isEqualTo(5000L);
        assertThat(result.otherIncomeBreakdown()).hasSize(1);
        assertThat(result.otherIncomeBreakdown().get(0).annualAmount()).isEqualTo(3000L);
    }

    @Test
    void buildsHistoricalSeries() {
        Pension p = new Pension();
        p.setPensionId(1L);
        p.setName("Alpha");
        p.setColor("#336699");
        p.setStatus(PensionStatus.ACTIVE);
        when(pensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of(p));

        PensionStatement s1 = new PensionStatement();
        s1.setPlanValue(80000L);
        s1.setStatementDate(LocalDate.of(2025, 6, 1));
        PensionStatement s2 = new PensionStatement();
        s2.setPlanValue(100000L);
        s2.setStatementDate(LocalDate.of(2026, 1, 1));
        when(statementRepository.findByPensionIdOrderByStatementDateAsc(1L)).thenReturn(List.of(s1, s2));

        when(statePensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());
        when(otherIncomeRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of());
        when(userSettingsRepository.findByUserId(42L)).thenReturn(Optional.empty());

        AnalyticsDto result = service.getForUser(42L);

        // Pension series + Total series
        assertThat(result.pensionHistorySeries()).hasSize(2);
        AnalyticsDto.PensionHistorySeries alphaSeries = result.pensionHistorySeries().get(0);
        assertThat(alphaSeries.name()).isEqualTo("Alpha");
        assertThat(alphaSeries.dataPoints()).hasSize(2);

        AnalyticsDto.PensionHistorySeries totalSeries = result.pensionHistorySeries().get(1);
        assertThat(totalSeries.name()).isEqualTo("Total");
        assertThat(totalSeries.dataPoints()).hasSize(2);
        assertThat(totalSeries.dataPoints().get(1).value()).isEqualTo(100000L);
    }
}
