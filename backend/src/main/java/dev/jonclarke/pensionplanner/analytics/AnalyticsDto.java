package dev.jonclarke.pensionplanner.analytics;

import java.time.LocalDate;
import java.util.List;

public record AnalyticsDto(
        LocalDate today,
        LocalDate retirementDate,
        Long targetIncome,
        List<YearlyProjection> projections,
        List<PensionGrowthCost> pensionGrowthCosts,
        List<PensionIncomeBreakdown> pensionIncomeBreakdown,
        List<StatePensionBreakdown> statePensionBreakdown,
        List<OtherIncomeBreakdown> otherIncomeBreakdown,
        List<PensionHistorySeries> pensionHistorySeries
) {
    public record YearlyProjection(
            Integer year,
            Long totalValue
    ) {
    }

    public record PensionGrowthCost(
            String name,
            String color,
            Long growthValue,
            Long cumulativeCharges
    ) {
    }

    public record PensionIncomeBreakdown(
            String name,
            String color,
            Long projectedAnnualAmount
    ) {
    }

    public record StatePensionBreakdown(
            String name,
            Long yearlyAmount
    ) {
    }

    public record OtherIncomeBreakdown(
            String name,
            Long annualAmount
    ) {
    }

    public record PensionHistorySeries(
            String name,
            String color,
            List<DataPoint> dataPoints
    ) {
    }

    public record DataPoint(
            LocalDate date,
            Long value
    ) {
    }
}
