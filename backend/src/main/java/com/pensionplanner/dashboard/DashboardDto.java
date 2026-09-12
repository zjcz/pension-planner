package com.pensionplanner.dashboard;

import java.time.LocalDate;
import java.util.List;

public record DashboardDto(
        Long totalPortfolioValue,
        Long totalProjectedAnnualIncome,
        Long targetIncome,
        List<StatePensionSummary> statePensions,
        List<OtherIncomeSummary> otherIncome,
        LocalDate retirementDate
) {
    public record StatePensionSummary(
            Long id,
            String name,
            Long yearlyAmount,
            Integer takesEffectYear
    ) {
    }

    public record OtherIncomeSummary(
            Long id,
            String name,
            Long annualAmount
    ) {
    }
}