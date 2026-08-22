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
        return otherIncomeService.listForUser(currentUserService.currentUserId()).stream()
                .map(OtherIncomeDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public OtherIncomeDto get(@PathVariable Long id) {
        return OtherIncomeDto.from(otherIncomeService.getForUser(currentUserService.currentUserId(), id));
    }

    @PostMapping
    public ResponseEntity<OtherIncomeDto> create(@Valid @RequestBody OtherIncomeRequest request) {
        OtherIncomeDto dto = OtherIncomeDto.from(otherIncomeService.create(currentUserService.currentUserId(), request));
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    public OtherIncomeDto update(@PathVariable Long id, @Valid @RequestBody OtherIncomeRequest request) {
        return OtherIncomeDto.from(otherIncomeService.update(currentUserService.currentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        otherIncomeService.delete(currentUserService.currentUserId(), id);
    }
}
