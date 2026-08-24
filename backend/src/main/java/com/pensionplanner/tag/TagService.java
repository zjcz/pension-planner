package com.pensionplanner.tag;

import com.pensionplanner.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final PensionTagRepository pensionTagRepository;
    private final OtherIncomeTagRepository otherIncomeTagRepository;

    public TagService(TagRepository tagRepository,
                      PensionTagRepository pensionTagRepository,
                      OtherIncomeTagRepository otherIncomeTagRepository) {
        this.tagRepository = tagRepository;
        this.pensionTagRepository = pensionTagRepository;
        this.otherIncomeTagRepository = otherIncomeTagRepository;
    }

    @Transactional(readOnly = true)
    public List<Tag> listForUser(Long userId) {
        return tagRepository.findByUserIdOrderByNameAsc(userId);
    }

    @Transactional
    public Tag create(Long userId, String name) {
        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(name.trim());
        return tagRepository.save(tag);
    }

    @Transactional
    public void delete(Long userId, Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tag not found"));
        if (!tag.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Tag not found");
        }
        pensionTagRepository.deleteAllInBatch(pensionTagRepository.findByTagId(tagId));
        otherIncomeTagRepository.deleteAllInBatch(otherIncomeTagRepository.findByTagId(tagId));
        tagRepository.delete(tag);
    }
}
