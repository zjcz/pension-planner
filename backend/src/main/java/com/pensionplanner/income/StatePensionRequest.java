package com.pensionplanner.income;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StatePensionRequest(
        @NotNull(message = "yearlyAmount is required")
        @Min(value = 0, message = "yearlyAmount must be ≥ 0")
        Long yearlyAmount,

        @NotNull(message = "takesEffectYear is required")
        @Min(value = 2024, message = "takesEffectYear must be ≥ 2024")
        Integer takesEffectYear
) {
}
