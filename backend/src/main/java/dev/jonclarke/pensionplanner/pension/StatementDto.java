package dev.jonclarke.pensionplanner.pension;

import java.time.LocalDate;

public record StatementDto(
        Long statementId,
        Long pensionId,
        LocalDate statementDate,
        Long planValue,
        Long projectedAnnualAmount,
        Long yearlyCharges,
        Long transferValue,
        Long amountPaidIn,
        String statementNotes
) {
    public static StatementDto from(PensionStatement statement) {
        return new StatementDto(
                statement.getStatementId(),
                statement.getPensionId(),
                statement.getStatementDate(),
                statement.getPlanValue(),
                statement.getProjectedAnnualAmount(),
                statement.getYearlyCharges(),
                statement.getTransferValue(),
                statement.getAmountPaidIn(),
                statement.getStatementNotes()
        );
    }
}
