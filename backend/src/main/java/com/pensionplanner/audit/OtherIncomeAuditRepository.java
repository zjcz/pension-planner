package com.pensionplanner.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtherIncomeAuditRepository extends JpaRepository<OtherIncomeAudit, Long> {

    List<OtherIncomeAudit> findByIdOrderByAuditTimestampDesc(Long id);

    void deleteByUserId(Long userId);
}
