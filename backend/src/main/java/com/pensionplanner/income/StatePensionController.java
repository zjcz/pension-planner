package com.pensionplanner.income;

import com.pensionplanner.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/state-pension")
public class StatePensionController {

    private final StatePensionService statePensionService;
    private final CurrentUserService currentUserService;

    public StatePensionController(StatePensionService statePensionService, CurrentUserService currentUserService) {
        this.statePensionService = statePensionService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public StatePensionDto get() {
        Long userId = currentUserService.currentUserId();
        StatePension statePension = statePensionService.getForUser(userId);
        return statePension != null ? StatePensionDto.from(statePension) : null;
    }

    @PutMapping
    public StatePensionDto upsert(@Valid @RequestBody StatePensionRequest request) {
        Long userId = currentUserService.currentUserId();
        return StatePensionDto.from(statePensionService.upsert(userId, request));
    }
}
