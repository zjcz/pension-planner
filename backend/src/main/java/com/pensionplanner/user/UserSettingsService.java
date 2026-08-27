package com.pensionplanner.user;

import com.pensionplanner.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UserSettingsService {

    private final UserSettingsRepository repository;

    public UserSettingsService(UserSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public UserSettings getForUser(Long userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Settings not found"));
    }

    @Transactional
    public UserSettings update(Long userId, Long targetIncome, LocalDate retirementDate, boolean auditEnabled) {
        UserSettings settings = repository.findByUserId(userId).orElseGet(() -> {
            UserSettings created = new UserSettings();
            created.setUserId(userId);
            return created;
        });
        settings.setTargetIncome(targetIncome);
        settings.setRetirementDate(retirementDate);
        settings.setAuditEnabled(auditEnabled);
        return repository.save(settings);
    }
}
