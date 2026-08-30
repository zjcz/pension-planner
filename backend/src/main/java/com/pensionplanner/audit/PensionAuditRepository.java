package com.pensionplanner.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PensionAuditRepository extends JpaRepository<PensionAudit, Long> {

    List<PensionAudit> findByPensionIdOrderByAuditTimestampDesc(Long pensionId);

    void deleteByUserId(Long userId);
}
