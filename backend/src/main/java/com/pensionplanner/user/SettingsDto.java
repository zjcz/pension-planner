package com.pensionplanner.user;

import java.time.LocalDate;

public record SettingsDto(Long targetIncome, LocalDate retirementDate) {

    public static SettingsDto from(UserSettings settings) {
        return new SettingsDto(settings.getTargetIncome(), settings.getRetirementDate());
    }
}