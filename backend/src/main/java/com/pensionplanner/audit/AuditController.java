package com.pensionplanner.audit;

import com.pensionplanner.security.CurrentUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditReadService auditReadService;
    private final CurrentUserService currentUserService;

    public AuditController(AuditReadService auditReadService, CurrentUserService currentUserService) {
        this.auditReadService = auditReadService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/pensions/{pensionId}")
    public List<PensionAuditDto> pension(@PathVariable Long pensionId) {
        return auditReadService.getPensionAudit(currentUserService.currentUserId(), pensionId);
    }

    @GetMapping("/statements/{statementId}")
    public List<PensionStatementAuditDto> statement(@PathVariable Long statementId) {
        return auditReadService.getStatementAudit(currentUserService.currentUserId(), statementId);
    }

    @GetMapping("/other-income/{otherIncomeId}")
    public List<OtherIncomeAuditDto> otherIncome(@PathVariable Long otherIncomeId) {
        return auditReadService.getOtherIncomeAudit(currentUserService.currentUserId(), otherIncomeId);
    }

    @GetMapping("/state-pensions/{id}")
    public List<StatePensionAuditDto> statePension(@PathVariable Long id) {
        return auditReadService.getStatePensionAudit(currentUserService.currentUserId(), id);
    }
}
