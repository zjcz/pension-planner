package com.pensionplanner.pension;

import com.pensionplanner.audit.AuditService;
import com.pensionplanner.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PensionService {

    private final PensionRepository pensionRepository;
    private final PensionStatementRepository statementRepository;
    private final AuditService auditService;

    public PensionService(PensionRepository pensionRepository,
                          PensionStatementRepository statementRepository,
                          AuditService auditService) {
        this.pensionRepository = pensionRepository;
        this.statementRepository = statementRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<Pension> listForUser(Long userId) {
        return pensionRepository.findByUserIdOrderByNameAsc(userId);
    }

    @Transactional(readOnly = true)
    public Pension getForUser(Long userId, Long pensionId) {
        return pensionRepository.findByPensionIdAndUserId(pensionId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pension not found"));
    }

    @Transactional
    public Pension create(Long userId, PensionRequest request) {
        Pension pension = new Pension();
        pension.setUserId(userId);
        apply(pension, request);
        Pension saved = pensionRepository.save(pension);
        auditService.recordCreate(saved);
        return saved;
    }

    @Transactional
    public Pension update(Long userId, Long pensionId, PensionRequest request) {
        Pension pension = getForUser(userId, pensionId);
        auditService.recordUpdate(pension);
        apply(pension, request);
        return pensionRepository.save(pension);
    }

    @Transactional
    public void delete(Long userId, Long pensionId) {
        Pension pension = getForUser(userId, pensionId);
        List<PensionStatement> statements = statementRepository.findByPensionIdOrderByStatementDateAsc(pensionId);
        for (PensionStatement statement : statements) {
            auditService.recordDelete(statement);
        }
        if (!statements.isEmpty()) {
            statementRepository.deleteAll(statements);
        }
        auditService.recordDelete(pension);
        pensionRepository.delete(pension);
    }

    private void apply(Pension pension, PensionRequest request) {
        pension.setName(request.name().trim());
        pension.setMaturityDate(request.maturityDate());
        pension.setNotes(request.notes());
        pension.setColor(request.color());
        if (pension.getPensionId() == null) {
            pension.setStatusDate(LocalDate.now());
        } else if (pension.getStatus() != request.status()) {
            pension.setStatusDate(LocalDate.now());
        }
        pension.setStatus(request.status());
    }
}
