package dev.jonclarke.pensionplanner.income;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StatePensionRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        @NotNull(message = "yearlyAmount is required")
        @Min(value = 0, message = "yearlyAmount must be ≥ 0")
        Long yearlyAmount,

        @NotNull(message = "takesEffectYear is required")
        @Min(value = 2024, message = "takesEffectYear must be ≥ 2024")
        Integer takesEffectYear,

        String notes
) {
}