package com.pensionplanner.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PensionTagRepository extends JpaRepository<PensionTag, Long> {

    List<PensionTag> findByPensionId(Long pensionId);

    List<PensionTag> findByPensionIdIn(List<Long> pensionIds);

    List<PensionTag> findByTagId(Long tagId);

    void deleteByPensionId(Long pensionId);

    void deleteByPensionIdIn(List<Long> pensionIds);
}
