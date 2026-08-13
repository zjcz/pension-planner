package com.pensionplanner.user;

public record SettingsDto(Double targetIncome, java.time.Instant retirementDate) {

    public static SettingsDto from(UserSettings settings) {
        return new SettingsDto(settings.getTargetIncome(), settings.getRetirementDate());
    }
}
