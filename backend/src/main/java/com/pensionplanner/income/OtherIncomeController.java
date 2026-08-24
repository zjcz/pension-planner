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
@RequestMapping("/api/v1/other-income")
public class OtherIncomeController {

    private final OtherIncomeService otherIncomeService;
    private final CurrentUserService currentUserService;

    public OtherIncomeController(OtherIncomeService otherIncomeService, CurrentUserService currentUserService) {
        this.otherIncomeService = otherIncomeService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<OtherIncomeDto> list() {
        Long userId = currentUserService.currentUserId();
        List<OtherIncome> items = otherIncomeService.listForUser(userId);
        return items.stream()
                .map(oi -> OtherIncomeDto.from(oi, otherIncomeService.getTagsForOtherIncome(oi.getId())))
                .toList();
    }

    @GetMapping("/{id}")
    public OtherIncomeDto get(@PathVariable Long id) {
        Long userId = currentUserService.currentUserId();
        OtherIncome oi = otherIncomeService.getForUser(userId, id);
        return OtherIncomeDto.from(oi, otherIncomeService.getTagsForOtherIncome(id));
    }

    @PostMapping
    public ResponseEntity<OtherIncomeDto> create(@Valid @RequestBody OtherIncomeRequest request) {
        Long userId = currentUserService.currentUserId();
        OtherIncome oi = otherIncomeService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OtherIncomeDto.from(oi, otherIncomeService.getTagsForOtherIncome(oi.getId())));
    }

    @PutMapping("/{id}")
    public OtherIncomeDto update(@PathVariable Long id, @Valid @RequestBody OtherIncomeRequest request) {
        Long userId = currentUserService.currentUserId();
        OtherIncome oi = otherIncomeService.update(userId, id, request);
        return OtherIncomeDto.from(oi, otherIncomeService.getTagsForOtherIncome(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        otherIncomeService.delete(currentUserService.currentUserId(), id);
    }
}
