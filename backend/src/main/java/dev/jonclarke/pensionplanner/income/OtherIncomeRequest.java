package dev.jonclarke.pensionplanner.income;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OtherIncomeRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        @Min(value = 0, message = "annualAmount must be ≥ 0")
        Long annualAmount,

        String notes,

        List<Long> tagIds
) {
}
