package com.pensionplanner.pension;

import com.pensionplanner.audit.AuditService;
import com.pensionplanner.common.ApiException;
import com.pensionplanner.tag.PensionTag;
import com.pensionplanner.tag.PensionTagRepository;
import com.pensionplanner.tag.Tag;
import com.pensionplanner.tag.TagRepository;
import com.pensionplanner.tag.TagDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PensionService {

    private final PensionRepository pensionRepository;
    private final PensionStatementRepository statementRepository;
    private final PensionTagRepository pensionTagRepository;
    private final TagRepository tagRepository;
    private final AuditService auditService;

    public PensionService(PensionRepository pensionRepository,
                          PensionStatementRepository statementRepository,
                          PensionTagRepository pensionTagRepository,
                          TagRepository tagRepository,
                          AuditService auditService) {
        this.pensionRepository = pensionRepository;
        this.statementRepository = statementRepository;
        this.pensionTagRepository = pensionTagRepository;
        this.tagRepository = tagRepository;
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
        syncTags(userId, saved.getPensionId(), request.tagIds());
        auditService.recordCreate(saved);
        return saved;
    }

    @Transactional
    public Pension update(Long userId, Long pensionId, PensionRequest request) {
        Pension pension = getForUser(userId, pensionId);
        auditService.recordUpdate(pension);
        apply(pension, request);
        syncTags(userId, pensionId, request.tagIds());
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
        pensionTagRepository.deleteAllInBatch(pensionTagRepository.findByPensionId(pensionId));
        auditService.recordDelete(pension);
        pensionRepository.delete(pension);
    }

    @Transactional(readOnly = true)
    public List<TagDto> getTagsForPension(Long pensionId) {
        List<PensionTag> pensionTags = pensionTagRepository.findByPensionId(pensionId);
        if (pensionTags.isEmpty()) {
            return List.of();
        }
        List<Long> tagIds = pensionTags.stream().map(PensionTag::getTagId).toList();
        return tagRepository.findAllById(tagIds).stream()
                .map(TagDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TagDto> getTagsForPensions(List<Long> pensionIds) {
        if (pensionIds.isEmpty()) {
            return List.of();
        }
        var allPensionTags = pensionTagRepository.findByPensionIdIn(pensionIds);
        var allTagIds = allPensionTags.stream().map(PensionTag::getTagId).distinct().toList();
        if (allTagIds.isEmpty()) {
            return List.of();
        }
        return tagRepository.findAllById(allTagIds).stream()
                .map(TagDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public java.util.Map<Long, List<TagDto>> getTagsForPensionsByPensionId(List<Long> pensionIds) {
        if (pensionIds.isEmpty()) {
            return java.util.Map.of();
        }
        var allPensionTags = pensionTagRepository.findByPensionIdIn(pensionIds);
        var allTagIds = allPensionTags.stream().map(PensionTag::getTagId).distinct().toList();
        if (allTagIds.isEmpty()) {
            return java.util.Map.of();
        }
        var allTags = tagRepository.findAllById(allTagIds).stream()
                .collect(java.util.stream.Collectors.toMap(Tag::getId, t -> t));
        var tagsByPension = new java.util.HashMap<Long, List<TagDto>>();
        for (var pt : allPensionTags) {
            tagsByPension.computeIfAbsent(pt.getPensionId(), k -> new java.util.ArrayList<>())
                    .add(TagDto.from(allTags.get(pt.getTagId())));
        }
        return tagsByPension;
    }

    private void syncTags(Long userId, Long pensionId, List<Long> tagIds) {
        pensionTagRepository.deleteAllInBatch(pensionTagRepository.findByPensionId(pensionId));
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<Tag> tags = tagRepository.findByIdInAndUserId(new ArrayList<>(tagIds), userId);
        for (Tag tag : tags) {
            PensionTag pt = new PensionTag();
            pt.setPensionId(pensionId);
            pt.setTagId(tag.getId());
            pensionTagRepository.save(pt);
        }
    }

    private void apply(Pension pension, PensionRequest request) {
        pension.setName(request.name().trim());
        pension.setMaturityDate(request.maturityDate());
        pension.setNotes(request.notes());
        pension.setColor(request.color());
        pension.setProviderName(request.providerName());
        pension.setPolicyNumber(request.policyNumber());
        pension.setWorkplaceName(request.workplaceName());
        if (pension.getPensionId() == null) {
            pension.setStatusDate(LocalDate.now());
        } else if (pension.getStatus() != request.status()) {
            pension.setStatusDate(LocalDate.now());
        }
        pension.setStatus(request.status());
    }
}
