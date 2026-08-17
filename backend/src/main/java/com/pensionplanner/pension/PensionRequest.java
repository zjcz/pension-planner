package com.pensionplanner.pension;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PensionRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        @NotNull(message = "maturityDate is required")
        LocalDate maturityDate,

        @NotNull(message = "status is required")
        PensionStatus status,

        String notes,

        @Pattern(regexp = "^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{3})$", message = "color must be a hex value like #RRGGBB")
        String color
) {
}
