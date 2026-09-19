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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DashboardService {

    private final PensionRepository pensionRepository;
    private final PensionStatementRepository statementRepository;
    private final StatePensionRepository statePensionRepository;
    private final OtherIncomeRepository otherIncomeRepository;
    private final UserSettingsRepository userSettingsRepository;

    public DashboardService(PensionRepository pensionRepository,
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
    public DashboardDto getForUser(Long userId) {
        List<Pension> pensions = pensionRepository.findByUserIdOrderByNameAsc(userId);

        Map<Long, Optional<PensionStatement>> latestStatements = pensions.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Pension::getPensionId,
                        p -> statementRepository.findByPensionIdOrderByStatementDateAsc(p.getPensionId())
                                .stream()
                                .max(Comparator.comparing(PensionStatement::getStatementDate))
                ));

        long totalPortfolioValue = 0;
        long totalProjectedAnnualIncome = 0;

        for (Pension pension : pensions) {
            if (pension.getStatus() == PensionStatus.ACTIVE) {
                Optional<PensionStatement> latest = latestStatements.get(pension.getPensionId());
                if (latest.isPresent()) {
                    totalPortfolioValue += latest.get().getPlanValue();
                    totalProjectedAnnualIncome += latest.get().getProjectedAnnualAmount();
                }
            }
        }

        List<StatePension> statePensions = statePensionRepository.findByUserIdOrderByNameAsc(userId);
        List<DashboardDto.StatePensionSummary> statePensionSummaries = statePensions.stream()
                .map(sp -> new DashboardDto.StatePensionSummary(sp.getId(), sp.getName(), sp.getYearlyAmount(), sp.getTakesEffectYear()))
                .toList();
        totalProjectedAnnualIncome += statePensions.stream().mapToLong(StatePension::getYearlyAmount).sum();

        List<OtherIncome> otherIncomes = otherIncomeRepository.findByUserIdOrderByNameAsc(userId);
        List<DashboardDto.OtherIncomeSummary> otherIncomeSummaries = otherIncomes.stream()
                .map(oi -> new DashboardDto.OtherIncomeSummary(oi.getId(), oi.getName(), oi.getAnnualAmount()))
                .toList();
        totalProjectedAnnualIncome += otherIncomes.stream().mapToLong(OtherIncome::getAnnualAmount).sum();

        UserSettings settings = userSettingsRepository.findByUserId(userId).orElse(null);

        return new DashboardDto(
                totalPortfolioValue,
                totalProjectedAnnualIncome,
                settings != null ? settings.getTargetIncome() : null,
                statePensionSummaries,
                otherIncomeSummaries,
                settings != null ? settings.getRetirementDate() : null
        );
    }
}