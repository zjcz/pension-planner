package dev.jonclarke.pensionplanner.pension;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

public record StatementRequest(
        @NotNull(message = "statementDate is required")
        LocalDate statementDate,

        @NotNull(message = "planValue is required")
        Long planValue,

        @NotNull(message = "projectedAnnualAmount is required")
        Long projectedAnnualAmount,

        Long yearlyCharges,

        Long transferValue,

        Long amountPaidIn,

        @Length(max = 500)
        String statementNotes
) {
}
