package com.pensionplanner.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PensionStatementAuditRepository extends JpaRepository<PensionStatementAudit, Long> {

    List<PensionStatementAudit> findByStatementIdOrderByAuditTimestampDesc(Long statementId);
}
