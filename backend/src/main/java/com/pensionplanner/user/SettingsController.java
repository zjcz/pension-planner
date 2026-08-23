package com.pensionplanner.user;

import com.pensionplanner.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final UserSettingsService settingsService;
    private final CurrentUserService currentUserService;

    public SettingsController(UserSettingsService settingsService, CurrentUserService currentUserService) {
        this.settingsService = settingsService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public SettingsDto get() {
        return SettingsDto.from(settingsService.getForUser(currentUserService.currentUserId()));
    }

    @PutMapping
    public SettingsDto update(@Valid @RequestBody SettingsRequest request) {
        return SettingsDto.from(settingsService.update(
                currentUserService.currentUserId(), request.targetIncome(), request.retirementDate()));
    }
}
