package com.pensionplanner.audit;

import java.time.Instant;
import java.time.LocalDate;

public record PensionStatementAuditDto(
        Long auditId,
        String action,
        Instant auditTimestamp,
        Long statementId,
        LocalDate statementDate,
        Long planValue,
        Long projectedAnnualAmount,
        Long yearlyCharges,
        Long transferValue,
        Long amountPaidIn,
        String statementNotes
) {

    public static PensionStatementAuditDto from(PensionStatementAudit audit) {
        return new PensionStatementAuditDto(
                audit.getAuditId(),
                audit.getAction(),
                audit.getAuditTimestamp(),
                audit.getStatementId(),
                audit.getStatementDate(),
                audit.getPlanValue(),
                audit.getProjectedAnnualAmount(),
                audit.getYearlyCharges(),
                audit.getTransferValue(),
                audit.getAmountPaidIn(),
                audit.getStatementNotes());
    }
}
