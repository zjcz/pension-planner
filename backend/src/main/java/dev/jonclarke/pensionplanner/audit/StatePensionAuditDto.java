package dev.jonclarke.pensionplanner.audit;

import java.time.Instant;

public record StatePensionAuditDto(
        Long auditId,
        String action,
        Instant auditTimestamp,
        Long id,
        String name,
        Long yearlyAmount,
        Integer takesEffectYear,
        String notes
) {

    public static StatePensionAuditDto from(StatePensionAudit audit) {
        return new StatePensionAuditDto(
                audit.getAuditId(),
                audit.getAction(),
                audit.getAuditTimestamp(),
                audit.getId(),
                audit.getName(),
                audit.getYearlyAmount(),
                audit.getTakesEffectYear(),
                audit.getNotes());
    }
}