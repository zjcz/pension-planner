package com.pensionplanner.income;

public record StatePensionDto(
        Long id,
        String name,
        Long yearlyAmount,
        Integer takesEffectYear,
        String notes
) {
    public static StatePensionDto from(StatePension statePension) {
        return new StatePensionDto(
                statePension.getId(),
                statePension.getName(),
                statePension.getYearlyAmount(),
                statePension.getTakesEffectYear(),
                statePension.getNotes()
        );
    }
}