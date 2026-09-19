package dev.jonclarke.pensionplanner.analytics;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final PensionRepository pensionRepository;
    private final PensionStatementRepository statementRepository;
    private final StatePensionRepository statePensionRepository;
    private final OtherIncomeRepository otherIncomeRepository;
    private final UserSettingsRepository userSettingsRepository;

    public AnalyticsService(PensionRepository pensionRepository,
                            PensionStatementRepository statementRepository,
                            StatePensionRepository statePensionRepository,
                            OtherIncomeRepository otherIncomeRepository,
                            UserSettingsRepository userSettingsRepository) {
        this.pensionRepository = pensionRepository;
        this.statementRepository = statementRepository;
        this.statePensionRepository = statePensionRepository;
        this.otherIncomeRepository = otherIncomeRepository;
        this.userSettingsRepository = userSettingsRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsDto getForUser(Long userId) {
        List<Pension> pensions = pensionRepository.findByUserIdOrderByNameAsc(userId);
        LocalDate today = LocalDate.now();

        Map<Long, List<PensionStatement>> statementsByPension = new LinkedHashMap<>();
        for (Pension p : pensions) {
            statementsByPension.put(p.getPensionId(),
                    statementRepository.findByPensionIdOrderByStatementDateAsc(p.getPensionId()));
        }

        // Latest statement per pension
        Map<Long, Optional<PensionStatement>> latestByPension = statementsByPension.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().max(Comparator.comparing(PensionStatement::getStatementDate))));

        // --- Projections (linear extrapolation for active pensions) ---
        UserSettings settings = userSettingsRepository.findByUserId(userId).orElse(null);
        LocalDate retirementDate = settings != null ? settings.getRetirementDate() : null;
        Long targetIncome = settings != null ? settings.getTargetIncome() : null;

        long totalCurrentPortfolio = 0;
        long totalProjectedAnnual = 0;
        for (Pension p : pensions) {
            if (p.getStatus() == PensionStatus.ACTIVE) {
                Optional<PensionStatement> latest = latestByPension.get(p.getPensionId());
                if (latest.isPresent()) {
                    totalCurrentPortfolio += latest.get().getPlanValue() != null ? latest.get().getPlanValue() : 0;
                    totalProjectedAnnual += latest.get().getProjectedAnnualAmount() != null ? latest.get().getProjectedAnnualAmount() : 0;
                }
            }
        }

        List<AnalyticsDto.YearlyProjection> projections = new ArrayList<>();
        if (retirementDate != null && totalProjectedAnnual > 0) {
            long runningTotal = totalCurrentPortfolio;
            int startYear = today.getYear();
            int endYear = retirementDate.getYear();
            projections.add(new AnalyticsDto.YearlyProjection(startYear, runningTotal));
            for (int year = startYear + 1; year <= endYear; year++) {
                runningTotal += totalProjectedAnnual;
                projections.add(new AnalyticsDto.YearlyProjection(year, runningTotal));
            }
        } else if (totalCurrentPortfolio > 0) {
            projections.add(new AnalyticsDto.YearlyProjection(today.getYear(), totalCurrentPortfolio));
        }

        // --- Growth vs. Cost ---
        List<AnalyticsDto.PensionGrowthCost> growthCosts = new ArrayList<>();
        for (Pension p : pensions) {
            List<PensionStatement> stmts = statementsByPension.getOrDefault(p.getPensionId(), List.of());
            if (stmts.isEmpty()) continue;

            long totalPaidIn = stmts.stream()
                    .mapToLong(s -> s.getAmountPaidIn() != null ? s.getAmountPaidIn() : 0)
                    .sum();
            long totalCharges = stmts.stream()
                    .mapToLong(s -> s.getYearlyCharges() != null ? s.getYearlyCharges() : 0)
                    .sum();
            long latestValue = stmts.stream()
                    .max(Comparator.comparing(PensionStatement::getStatementDate))
                    .map(PensionStatement::getPlanValue)
                    .orElse(0L);

            growthCosts.add(new AnalyticsDto.PensionGrowthCost(
                    p.getName(),
                    p.getColor(),
                    latestValue - totalPaidIn,
                    totalCharges));
        }

        // --- Income breakdown (for stacked bar) ---
        List<AnalyticsDto.PensionIncomeBreakdown> incomeBreakdown = new ArrayList<>();
        for (Pension p : pensions) {
            if (p.getStatus() == PensionStatus.ACTIVE) {
                Optional<PensionStatement> latest = latestByPension.get(p.getPensionId());
                Long projAmount = latest.isPresent() ? latest.get().getProjectedAnnualAmount() : null;
                if (projAmount != null && projAmount > 0) {
                    incomeBreakdown.add(new AnalyticsDto.PensionIncomeBreakdown(
                            p.getName(),
                            p.getColor(),
                            projAmount));
                }
            }
        }

        List<StatePension> statePensions = statePensionRepository.findByUserIdOrderByNameAsc(userId);
        List<AnalyticsDto.StatePensionBreakdown> statePensionBreakdown = statePensions.stream()
                .map(sp -> new AnalyticsDto.StatePensionBreakdown(sp.getName(), sp.getYearlyAmount()))
                .toList();

        List<OtherIncome> otherIncomes = otherIncomeRepository.findByUserIdOrderByNameAsc(userId);
        List<AnalyticsDto.OtherIncomeBreakdown> oiBreakdown = otherIncomes.stream()
                .map(oi -> new AnalyticsDto.OtherIncomeBreakdown(oi.getName(), oi.getAnnualAmount()))
                .toList();

        // --- Historical portfolio trend ---
        // Collect all unique statement dates across pensions
        Map<LocalDate, Map<Long, Long>> dateValues = new LinkedHashMap<>();
        for (Pension p : pensions) {
            for (PensionStatement s : statementsByPension.getOrDefault(p.getPensionId(), List.of())) {
                dateValues.computeIfAbsent(s.getStatementDate(), k -> new LinkedHashMap<>())
                        .put(p.getPensionId(), s.getPlanValue());
            }
        }

        List<AnalyticsDto.PensionHistorySeries> historySeries = new ArrayList<>();
        for (Pension p : pensions) {
            List<AnalyticsDto.DataPoint> points = new ArrayList<>();
            for (Map.Entry<LocalDate, Map<Long, Long>> entry : dateValues.entrySet()) {
                Long val = entry.getValue().get(p.getPensionId());
                if (val != null) {
                    points.add(new AnalyticsDto.DataPoint(entry.getKey(), val));
                }
            }
            if (!points.isEmpty()) {
                historySeries.add(new AnalyticsDto.PensionHistorySeries(p.getName(), p.getColor(), points));
            }
        }

        // Total series
        if (!dateValues.isEmpty()) {
            List<AnalyticsDto.DataPoint> totalPoints = new ArrayList<>();
            for (Map.Entry<LocalDate, Map<Long, Long>> entry : dateValues.entrySet()) {
                long total = entry.getValue().values().stream().mapToLong(Long::longValue).sum();
                totalPoints.add(new AnalyticsDto.DataPoint(entry.getKey(), total));
            }
            historySeries.add(new AnalyticsDto.PensionHistorySeries("Total", null, totalPoints));
        }

        return new AnalyticsDto(
                today,
                retirementDate,
                targetIncome,
                projections,
                growthCosts,
                incomeBreakdown,
                statePensionBreakdown,
                oiBreakdown,
                historySeries);
    }
}
