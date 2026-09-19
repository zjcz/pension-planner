package dev.jonclarke.pensionplanner.user;

import java.time.LocalDate;

public record SettingsDto(Long targetIncome, LocalDate retirementDate, boolean auditEnabled) {

    public static SettingsDto from(UserSettings settings) {
        return new SettingsDto(settings.getTargetIncome(), settings.getRetirementDate(), settings.isAuditEnabled());
    }
}