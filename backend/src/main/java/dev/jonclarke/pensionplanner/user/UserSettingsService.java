package dev.jonclarke.pensionplanner.user;

import dev.jonclarke.pensionplanner.audit.AuditService;
import dev.jonclarke.pensionplanner.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UserSettingsService {

    private final UserSettingsRepository repository;
    private final AuditService auditService;

    public UserSettingsService(UserSettingsRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
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
        boolean wasEnabled = settings.isAuditEnabled();
        settings.setTargetIncome(targetIncome);
        settings.setRetirementDate(retirementDate);
        settings.setAuditEnabled(auditEnabled);
        UserSettings saved = repository.save(settings);
        if (wasEnabled && !auditEnabled) {
            auditService.purgeUserAudits(userId);
        } else if (!wasEnabled && auditEnabled) {
            auditService.snapshotUserAudits(userId);
        }
        return saved;
    }
}
