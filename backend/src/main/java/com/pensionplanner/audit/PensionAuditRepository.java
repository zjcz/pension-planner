package com.pensionplanner.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PensionAuditRepository extends JpaRepository<PensionAudit, Long> {
}
