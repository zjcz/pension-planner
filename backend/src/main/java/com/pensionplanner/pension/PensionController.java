package com.pensionplanner.pension;

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
@RequestMapping("/api/v1/pensions")
public class PensionController {

    private final PensionService pensionService;
    private final CurrentUserService currentUserService;

    public PensionController(PensionService pensionService, CurrentUserService currentUserService) {
        this.pensionService = pensionService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<PensionDto> list() {
        return pensionService.listForUser(currentUserService.currentUserId()).stream()
                .map(PensionDto::from)
                .toList();
    }

    @GetMapping("/{pensionId}")
    public PensionDto get(@PathVariable Long pensionId) {
        return PensionDto.from(pensionService.getForUser(currentUserService.currentUserId(), pensionId));
    }

    @PostMapping
    public ResponseEntity<PensionDto> create(@Valid @RequestBody PensionRequest request) {
        PensionDto dto = PensionDto.from(pensionService.create(currentUserService.currentUserId(), request));
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{pensionId}")
    public PensionDto update(@PathVariable Long pensionId, @Valid @RequestBody PensionRequest request) {
        return PensionDto.from(pensionService.update(currentUserService.currentUserId(), pensionId, request));
    }

    @DeleteMapping("/{pensionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long pensionId) {
        pensionService.delete(currentUserService.currentUserId(), pensionId);
    }
}
