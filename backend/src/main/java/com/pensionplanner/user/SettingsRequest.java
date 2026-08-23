package com.pensionplanner.user;

import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public record SettingsRequest(
        @Min(value = 0, message = "targetIncome must be ≥ 0")
        Long targetIncome,

        LocalDate retirementDate
) {
}
