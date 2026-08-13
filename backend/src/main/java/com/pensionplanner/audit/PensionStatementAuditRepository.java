package com.pensionplanner.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PensionStatementAuditRepository extends JpaRepository<PensionStatementAudit, Long> {
}
