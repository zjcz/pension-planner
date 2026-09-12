package com.pensionplanner.income;

import com.pensionplanner.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public List<StatePensionDto> list() {
        Long userId = currentUserService.currentUserId();
        return statePensionService.listForUser(userId).stream()
                .map(StatePensionDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public StatePensionDto get(@PathVariable Long id) {
        Long userId = currentUserService.currentUserId();
        return StatePensionDto.from(statePensionService.getForUser(userId, id));
    }

    @PostMapping
    public ResponseEntity<StatePensionDto> create(@Valid @RequestBody StatePensionRequest request) {
        Long userId = currentUserService.currentUserId();
        StatePension statePension = statePensionService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(StatePensionDto.from(statePension));
    }

    @PutMapping("/{id}")
    public StatePensionDto update(@PathVariable Long id, @Valid @RequestBody StatePensionRequest request) {
        Long userId = currentUserService.currentUserId();
        return StatePensionDto.from(statePensionService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        statePensionService.delete(currentUserService.currentUserId(), id);
    }
}