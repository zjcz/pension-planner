package dev.jonclarke.pensionplanner.pension;

import dev.jonclarke.pensionplanner.security.CurrentUserService;
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
@RequestMapping("/api/v1/pensions/{pensionId}/statements")
public class StatementController {

    private final StatementService statementService;
    private final CurrentUserService currentUserService;

    public StatementController(StatementService statementService, CurrentUserService currentUserService) {
        this.statementService = statementService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<StatementDto> list(@PathVariable Long pensionId) {
        return statementService.listForPension(currentUserService.currentUserId(), pensionId)
                .stream()
                .map(StatementDto::from)
                .toList();
    }

    @GetMapping("/{statementId}")
    public StatementDto get(@PathVariable Long pensionId, @PathVariable Long statementId) {
        return StatementDto.from(statementService.getForUser(currentUserService.currentUserId(), pensionId, statementId));
    }

    @PostMapping
    public ResponseEntity<StatementDto> create(@PathVariable Long pensionId,
                                               @Valid @RequestBody StatementRequest statementRequest) {
        StatementDto dto = StatementDto.from(statementService.create(currentUserService.currentUserId(), pensionId, statementRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{statementId}")
    public StatementDto update(@PathVariable Long pensionId,
                               @PathVariable Long statementId,
                               @Valid @RequestBody StatementRequest statementRequest) {
        return StatementDto.from(statementService.update(currentUserService.currentUserId(), pensionId, statementId, statementRequest));
    }

    @DeleteMapping("/{statementId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long pensionId, @PathVariable Long statementId) {
        statementService.delete(currentUserService.currentUserId(), pensionId, statementId);
    }
}
