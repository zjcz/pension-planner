package com.pensionplanner.income;

import com.pensionplanner.audit.AuditService;
import com.pensionplanner.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OtherIncomeService {

    private final OtherIncomeRepository otherIncomeRepository;
    private final AuditService auditService;

    public OtherIncomeService(OtherIncomeRepository otherIncomeRepository, AuditService auditService) {
        this.otherIncomeRepository = otherIncomeRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<OtherIncome> listForUser(Long userId) {
        return otherIncomeRepository.findByUserIdOrderByNameAsc(userId);
    }

    @Transactional(readOnly = true)
    public OtherIncome getForUser(Long userId, Long id) {
        return otherIncomeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Other income not found"));
    }

    @Transactional
    public OtherIncome create(Long userId, OtherIncomeRequest request) {
        OtherIncome otherIncome = new OtherIncome();
        otherIncome.setUserId(userId);
        apply(otherIncome, request);
        OtherIncome saved = otherIncomeRepository.save(otherIncome);
        auditService.recordCreate(saved);
        return saved;
    }

    @Transactional
    public OtherIncome update(Long userId, Long id, OtherIncomeRequest request) {
        OtherIncome otherIncome = getForUser(userId, id);
        auditService.recordUpdate(otherIncome);
        apply(otherIncome, request);
        return otherIncomeRepository.save(otherIncome);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        OtherIncome otherIncome = getForUser(userId, id);
        auditService.recordDelete(otherIncome);
        otherIncomeRepository.delete(otherIncome);
    }

    private void apply(OtherIncome otherIncome, OtherIncomeRequest request) {
        otherIncome.setName(request.name().trim());
        otherIncome.setAnnualAmount(request.annualAmount());
        otherIncome.setNotes(request.notes());
    }
}
