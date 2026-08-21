package com.pensionplanner.dashboard;

import java.time.LocalDate;
import java.util.List;

public record DashboardDto(
        Long totalPortfolioValue,
        Long totalProjectedAnnualIncome,
        Long targetIncome,
        StatePensionSummary statePension,
        List<OtherIncomeSummary> otherIncome,
        LocalDate retirementDate
) {
    public record StatePensionSummary(
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
