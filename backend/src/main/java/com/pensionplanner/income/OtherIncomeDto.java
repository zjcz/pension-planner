package com.pensionplanner.income;

public record OtherIncomeDto(
        Long id,
        String name,
        Long annualAmount,
        String notes
) {
    public static OtherIncomeDto from(OtherIncome otherIncome) {
        return new OtherIncomeDto(
                otherIncome.getId(),
                otherIncome.getName(),
                otherIncome.getAnnualAmount(),
                otherIncome.getNotes()
        );
    }
}
