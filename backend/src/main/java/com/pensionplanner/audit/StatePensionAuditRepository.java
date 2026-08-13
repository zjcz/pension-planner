package com.pensionplanner.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StatePensionAuditRepository extends JpaRepository<StatePensionAudit, Long> {
}
