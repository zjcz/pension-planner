package com.pensionplanner.income;

import com.pensionplanner.audit.AuditService;
import com.pensionplanner.common.ApiException;
import com.pensionplanner.tag.OtherIncomeTag;
import com.pensionplanner.tag.OtherIncomeTagRepository;
import com.pensionplanner.tag.Tag;
import com.pensionplanner.tag.TagRepository;
import com.pensionplanner.tag.TagDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OtherIncomeService {

    private final OtherIncomeRepository otherIncomeRepository;
    private final OtherIncomeTagRepository otherIncomeTagRepository;
    private final TagRepository tagRepository;
    private final AuditService auditService;

    public OtherIncomeService(OtherIncomeRepository otherIncomeRepository,
                              OtherIncomeTagRepository otherIncomeTagRepository,
                              TagRepository tagRepository,
                              AuditService auditService) {
        this.otherIncomeRepository = otherIncomeRepository;
        this.otherIncomeTagRepository = otherIncomeTagRepository;
        this.tagRepository = tagRepository;
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
        syncTags(userId, saved.getId(), request.tagIds());
        auditService.recordCreate(saved);
        return saved;
    }

    @Transactional
    public OtherIncome update(Long userId, Long id, OtherIncomeRequest request) {
        OtherIncome otherIncome = getForUser(userId, id);
        auditService.recordUpdate(otherIncome);
        apply(otherIncome, request);
        syncTags(userId, id, request.tagIds());
        return otherIncomeRepository.save(otherIncome);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        OtherIncome otherIncome = getForUser(userId, id);
        auditService.recordDelete(otherIncome);
        otherIncomeTagRepository.deleteAllInBatch(otherIncomeTagRepository.findByOtherIncomeId(id));
        otherIncomeRepository.delete(otherIncome);
    }

    @Transactional(readOnly = true)
    public List<TagDto> getTagsForOtherIncome(Long otherIncomeId) {
        List<OtherIncomeTag> oiTags = otherIncomeTagRepository.findByOtherIncomeId(otherIncomeId);
        if (oiTags.isEmpty()) {
            return List.of();
        }
        List<Long> tagIds = oiTags.stream().map(OtherIncomeTag::getTagId).toList();
        return tagRepository.findAllById(tagIds).stream()
                .map(TagDto::from)
                .toList();
    }

    private void syncTags(Long userId, Long otherIncomeId, List<Long> tagIds) {
        otherIncomeTagRepository.deleteAllInBatch(otherIncomeTagRepository.findByOtherIncomeId(otherIncomeId));
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<Tag> tags = tagRepository.findByIdInAndUserId(new ArrayList<>(tagIds), userId);
        for (Tag tag : tags) {
            OtherIncomeTag oit = new OtherIncomeTag();
            oit.setOtherIncomeId(otherIncomeId);
            oit.setTagId(tag.getId());
            otherIncomeTagRepository.save(oit);
        }
    }

    private void apply(OtherIncome otherIncome, OtherIncomeRequest request) {
        otherIncome.setName(request.name().trim());
        otherIncome.setAnnualAmount(request.annualAmount());
        otherIncome.setNotes(request.notes());
    }
}
