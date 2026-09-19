package dev.jonclarke.pensionplanner.audit;

import java.time.Instant;

public record OtherIncomeAuditDto(
        Long auditId,
        String action,
        Instant auditTimestamp,
        Long id,
        String name,
        Long annualAmount,
        String notes,
        String tags
) {

    public static OtherIncomeAuditDto from(OtherIncomeAudit audit) {
        return new OtherIncomeAuditDto(
                audit.getAuditId(),
                audit.getAction(),
                audit.getAuditTimestamp(),
                audit.getId(),
                audit.getName(),
                audit.getAnnualAmount(),
                audit.getNotes(),
                audit.getTags());
    }
}
