package com.pensionplanner.user;

import com.pensionplanner.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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
    public UserSettings update(Long userId, Double targetIncome, Instant retirementDate) {
        UserSettings settings = repository.findByUserId(userId).orElseGet(() -> {
            UserSettings created = new UserSettings();
            created.setUserId(userId);
            return created;
        });
        settings.setTargetIncome(targetIncome);
        settings.setRetirementDate(retirementDate);
        return repository.save(settings);
    }
}
