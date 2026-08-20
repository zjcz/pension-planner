package com.pensionplanner.income;

import com.pensionplanner.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatePensionService {

    private final StatePensionRepository statePensionRepository;
    private final AuditService auditService;

    public StatePensionService(StatePensionRepository statePensionRepository, AuditService auditService) {
        this.statePensionRepository = statePensionRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public StatePension getForUser(Long userId) {
        return statePensionRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public StatePension upsert(Long userId, StatePensionRequest request) {
        StatePension statePension = statePensionRepository.findByUserId(userId).orElse(null);
        if (statePension == null) {
            statePension = new StatePension();
            statePension.setUserId(userId);
            apply(statePension, request);
            StatePension saved = statePensionRepository.save(statePension);
            auditService.recordCreate(saved);
            return saved;
        }
        auditService.recordUpdate(statePension);
        apply(statePension, request);
        return statePensionRepository.save(statePension);
    }

    private void apply(StatePension statePension, StatePensionRequest request) {
        statePension.setYearlyAmount(request.yearlyAmount());
        statePension.setTakesEffectYear(request.takesEffectYear());
    }
}
