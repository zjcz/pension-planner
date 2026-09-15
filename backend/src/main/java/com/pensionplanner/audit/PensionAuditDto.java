package com.pensionplanner.audit;

import java.time.Instant;
import java.time.LocalDate;

public record PensionAuditDto(
        Long auditId,
        String action,
        Instant auditTimestamp,
        String name,
        LocalDate maturityDate,
        String notes,
        String status,
        LocalDate statusDate,
        String color,
        String providerName,
        String policyNumber,
        String workplaceName,
        String tags
) {

    public static PensionAuditDto from(PensionAudit audit) {
        return new PensionAuditDto(
                audit.getAuditId(),
                audit.getAction(),
                audit.getAuditTimestamp(),
                audit.getName(),
                audit.getMaturityDate(),
                audit.getNotes(),
                audit.getStatus(),
                audit.getStatusDate(),
                audit.getColor(),
                audit.getProviderName(),
                audit.getPolicyNumber(),
                audit.getWorkplaceName(),
                audit.getTags());
    }
}
