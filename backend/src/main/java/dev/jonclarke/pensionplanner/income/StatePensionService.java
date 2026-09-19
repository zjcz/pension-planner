package dev.jonclarke.pensionplanner.income;

import dev.jonclarke.pensionplanner.audit.AuditService;
import dev.jonclarke.pensionplanner.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StatePensionService {

    private final StatePensionRepository statePensionRepository;
    private final AuditService auditService;

    public StatePensionService(StatePensionRepository statePensionRepository, AuditService auditService) {
        this.statePensionRepository = statePensionRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<StatePension> listForUser(Long userId) {
        return statePensionRepository.findByUserIdOrderByNameAsc(userId);
    }

    @Transactional(readOnly = true)
    public StatePension getForUser(Long userId, Long id) {
        return statePensionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "State pension not found"));
    }

    @Transactional
    public StatePension create(Long userId, StatePensionRequest request) {
        StatePension statePension = new StatePension();
        statePension.setUserId(userId);
        apply(statePension, request);
        StatePension saved = statePensionRepository.save(statePension);
        auditService.recordCreate(saved);
        return saved;
    }

    @Transactional
    public StatePension update(Long userId, Long id, StatePensionRequest request) {
        StatePension statePension = getForUser(userId, id);
        auditService.recordUpdate(statePension);
        apply(statePension, request);
        return statePensionRepository.save(statePension);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        StatePension statePension = getForUser(userId, id);
        auditService.recordDelete(statePension);
        statePensionRepository.delete(statePension);
    }

    private void apply(StatePension statePension, StatePensionRequest request) {
        statePension.setName(request.name().trim());
        statePension.setYearlyAmount(request.yearlyAmount());
        statePension.setTakesEffectYear(request.takesEffectYear());
        statePension.setNotes(request.notes());
    }
}